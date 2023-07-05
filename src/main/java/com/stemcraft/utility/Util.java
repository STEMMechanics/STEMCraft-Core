package com.stemcraft.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;

public class Util {
    public static String capitalize(String string, boolean allWords) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        if (string.length() <= 1) {
            return string.toUpperCase(Locale.ROOT);
        }

        string = string.toLowerCase(Locale.ROOT);

        if (allWords) {
            StringBuilder builder = new StringBuilder();
            for (String substring : string.split(" ")) {
                if (substring.length() >= 3 && !substring.equals("and") && !substring.equals("the")) {
                    substring = substring.substring(0, 1).toUpperCase(Locale.ROOT) + substring.substring(1);
                }
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(substring);
            }

            return builder.toString();
        }

        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
    }

    public static boolean isSafeLocation(Location location) {
        Block block = location.getBlock();
        Block aboveBlock = block.getRelative(BlockFace.UP);
        Block belowBlock = block.getRelative(BlockFace.DOWN);
        
        // Check if the block and the block above are air blocks
        if (block.getType() == Material.AIR && aboveBlock.getType() == Material.AIR && belowBlock.getType().isSolid()) {
            // // Check if the block and the block above are not water or lava blocks
            // if (block.getType() != Material.WATER && block.getType() != Material.LAVA &&
            //         aboveBlock.getType() != Material.WATER && aboveBlock.getType() != Material.LAVA) {
            //     // Check if the block and the block above are not solid blocks (excluding certain transparent blocks)
            //     if (!block.getType().isSolid() && !aboveBlock.getType().isSolid()) {
                    return true;
            //     }
            // }
        }

        return false;
    }

    public static Location findSafeLocation(Location location, int range) {
        return findSafeLocation(location, range, false);
    }
    
    public static Location findSafeLocation(Location location, int range, boolean random) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        List<Location> safeLocations = new ArrayList<>();

        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {
                for (int k = -range; k <= range; k++) {
                    Location checkLocation = new Location(world, x + i, y + j, z + k);
                    if (isSafeLocation(checkLocation)) {
                        if(!random) {
                            return checkLocation;
                        }

                        safeLocations.add(checkLocation);
                    }
                }
            }
        }

        if (!safeLocations.isEmpty()) {
            Random randomLoc = new Random();
            int randomIndex = randomLoc.nextInt(safeLocations.size());
            return safeLocations.get(randomIndex);
        }

        return null;
    }

    public static Player getPlayerByDisplayName(String displayName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getDisplayName().equalsIgnoreCase(displayName)) {
                return player;
            }
        }
        return null;
    }

    public static boolean loadChunkRadius(Location pos, int radius, boolean force) {
        int baseX = (int)(pos.getX() / 16.0D);
        int baseZ = (int)(pos.getZ() / 16.0D);
        boolean success = true;

        for(int x = -1 * radius; x <= radius; x++) {
            for(int z = -1 * radius; z <= radius; z++) {
                int chunkX = baseX + x;
                int chunkZ = baseZ + z;
                World world = pos.getWorld();

                try {
                    world.addPluginChunkTicket(chunkX, chunkZ, STEMCraft.getInstance());
                }
                catch (NoSuchMethodError e) {
                    /* empty */
                }

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                if(!world.isChunkLoaded(chunk)) {
                    boolean load = false;

                    try {
                        load = world.loadChunk(chunkX, chunkZ, true);
                    } catch (RuntimeException e) {
                        load = false;
                    }

                    if(!load) {
                        if(success) {
                            success = false;
                        }
                        System.out.println("Failed to load and activate Chunk at X: " + (chunkX * 16) + " Z: " + (chunkZ * 16) + " in " + world.getName());
                    }
                }
            }
        }

        return success;
    }
}
