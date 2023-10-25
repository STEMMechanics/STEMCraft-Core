package com.stemcraft.feature;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMTask;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.event.SMEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMChunkPruning extends SMFeature {
    private SMTask pruneTask = null;
    private Map<Chunk, Long> chunkCache = new HashMap<>();

    private static final int CHECK_INTERVAL_MINS = 5;

    @Override
    protected Boolean onEnable() {
        if(pruneTask != null) {
            pruneTask.cancel();
            pruneTask = null;
        }

        SMDatabase.runMigration("230805165900_CreateChunkPruneTable", () -> {
            SMDatabase.prepareStatement(
            "CREATE TABLE IF NOT EXISTS chunk_prune (" +
                "chunk TEXT PRIMARY KEY," +
                "last_visited INTEGER NOT NULL)").executeUpdate();
        });

        // this.pruneChunks();

        SMEvent.register(BlockBreakEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            queueChunk(player.getLocation().getChunk());
        });

        SMEvent.register(BlockPlaceEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            queueChunk(player.getLocation().getChunk());
        });

        SMEvent.register(PlayerInteractEvent.class, ctx -> {
            Block clickedBlock = ctx.event.getClickedBlock();

            if (clickedBlock == null) {
                return;
            }
    
            this.queueChunk(clickedBlock.getLocation().getChunk());
        });

        
        return true;
    }

    @Override
    public void onDisable() {
        if(pruneTask != null) {
            pruneTask.cancel();
            pruneTask = null;
        }
    }

    public boolean isRegionInChunk(Chunk chunk)
    {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(chunk.getWorld()));
        
        final double maxX = chunk.getX() * 16d;
        final double minX = maxX + 15d;
        
        final double maxZ = chunk.getZ() * 16d;
        final double minZ = maxZ + 15d;
        
        final BlockVector3 max = BlockVector3.at(maxX, 256, maxZ);
        final BlockVector3 min = BlockVector3.at(minX, 0, minZ);
        
        ProtectedRegion chunkRegion = new ProtectedCuboidRegion("chunkRegion", min, max);
        if(regions != null) {
            return chunkRegion.getIntersectingRegions(regions.getRegions().values()).isEmpty();
        }

        return true;
    }

    private void queueChunk(Chunk chunk) {
        if(!this.chunkCache.containsKey(chunk)) {
            if(!isRegionInChunk(chunk)) {
                this.chunkCache.put(chunk, System.currentTimeMillis());
            }
        }
    }

    private void pruneChunks() {
        // Save cache to DB
        Map<Chunk, Long> cache = new HashMap<>(this.chunkCache);
        this.chunkCache.clear();

        cache.forEach((chunk, millis) -> {
            try {
                String id = chunk.getX() + ";" + chunk.getZ() + ";" + chunk.getWorld().getName();

                PreparedStatement statement = SMDatabase.prepareStatement(
                        "INSERT OR REPLACE INTO chunk_prune (chunk, last_visited) VALUES (?, ?)"
                );
                statement.setString(1, id);
                statement.setLong(2, millis);
                statement.executeUpdate();
                statement.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        
        Boolean regeneratedChunk = false;

        // Destroy chunks if no players online
        if(STEMCraft.getPlugin().getServer().getOnlinePlayers().size() == 0) {
            List<String> chunksToPrune = new ArrayList<>();
            long aged = System.currentTimeMillis() - (86400000 * SMConfig.main().getInt("regenerate-chunk-delay"));

            try {
                PreparedStatement statement = SMDatabase.prepareStatement(
                    "SELECT chunk FROM chunk_prune WHERE last_visited < ?;"
                );
                statement.setLong(1, aged);

                ResultSet resultSet = statement.executeQuery();
                while(resultSet.next()) {
                    chunksToPrune.add(resultSet.getString("chunk"));
                }

                resultSet.close();
                statement.close();
            } catch(Exception e) {
                e.printStackTrace();
            }

            while(STEMCraft.getPlugin().getServer().getOnlinePlayers().size() == 0 && !chunksToPrune.isEmpty()) {
                String chunkStr = chunksToPrune.get(0);
                chunksToPrune.remove(0);

                String[] chunkData = chunkStr.split(";", 3);
                if(chunkData.length == 3) {
                    World world = STEMCraft.getPlugin().getServer().getWorld(chunkData[2]);
                    if(world != null) {
                        int chunkX = Integer.parseInt(chunkData[0]);
                        int chunkZ = Integer.parseInt(chunkData[1]);
                        if(world.isChunkLoaded(chunkX, chunkZ)) {
                            continue;
                        }

                        try {
                            PreparedStatement statement = SMDatabase.prepareStatement(
                                "DELETE FROM chunk_prune WHERE chunk = ?");
                            statement.setString(1, chunkStr);
                            statement.executeUpdate();

                            statement.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        if(world.isChunkGenerated(chunkX, chunkZ)) {
                            Chunk chunk = world.getChunkAt(chunkX, chunkZ);

                            if(chunk != null) {
                                this.regenerateChunk(chunk);
                                regeneratedChunk = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        int reschedule = (regeneratedChunk ? 5 : 60 * CHECK_INTERVAL_MINS);

        pruneTask = STEMCraft.runLater(reschedule, () -> {
            pruneChunks();
        });
    }

    private void regenerateChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        // Get the world
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

        // Create an EditSession
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build()) {

            // Define the chunk region
            BlockVector3 min = BlockVector3.at(chunkX * 16, 0, chunkZ * 16);
            BlockVector3 max = BlockVector3.at(chunkX * 16 + 15, world.getMaxHeight(), chunkZ * 16 + 15);
            CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world), min, max);

            RegenOptions options = RegenOptions.builder().regenBiomes(true).build();

            // Regenerate the chunk
            weWorld.regenerate(region, editSession, options);
        }

        STEMCraft.info("Chunk at X:" + chunkX + ", Z:" + chunkZ + " in world " + world.getName() + " has been regenerated.");
    }
}