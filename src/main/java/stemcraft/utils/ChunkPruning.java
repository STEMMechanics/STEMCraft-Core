import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.WorldBorder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkPruningPlugin extends JavaPlugin {

    private static final int CHECK_INTERVAL = 20 * 60 * 5; // Check every 5 minutes
    private static final int PLAYER_THRESHOLD_SECONDS = 2 * 60; // 2 minutes
    private static final int INACTIVE_THRESHOLD_DAYS = 14;

    private Map<UUID, Long> playerTimeMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getScheduler().runTaskTimer(this, this::checkInactiveChunks, CHECK_INTERVAL, CHECK_INTERVAL);
        getServer().getPluginManager().registerEvents(new PlayerTimeListener(), this);
    }

    @Override
    public void onDisable() {
        // Any cleanup code, if needed
    }

    private void checkInactiveChunks() {
        Bukkit.getWorlds().forEach(this::processWorld);
    }

    private void processWorld(World world) {
        for (Chunk chunk : world.getLoadedChunks()) {
            long totalPlayerTime = getPlayerTimeInChunk(chunk);

            if (totalPlayerTime <= PLAYER_THRESHOLD_SECONDS && isChunkInactive(chunk, INACTIVE_THRESHOLD_DAYS)) {
                regenerateChunk(chunk);
            }
        }
    }

    private long getPlayerTimeInChunk(Chunk chunk) {
        long totalPlayerTime = 0;
        World world = chunk.getWorld();

        for (Player player : world.getPlayers()) {
            if (player.getWorld().equals(world) && player.getLocation().getChunk().equals(chunk)) {
                totalPlayerTime += playerTimeMap.getOrDefault(player.getUniqueId(), 0L);
            }
        }

        return totalPlayerTime;
    }

    private boolean isChunkInactive(Chunk chunk, int inactiveThresholdDays) {
        long currentTime = System.currentTimeMillis();
        long lastPlayedTime = chunk.getLastPlayed();

        if (lastPlayedTime > 0) {
            long inactiveTime = currentTime - lastPlayedTime;
            long inactiveDays = inactiveTime / (1000 * 60 * 60 * 24);
            return inactiveDays >= inactiveThresholdDays;
        }

        return false;
    }

    private void regenerateChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        WorldBorder worldBorder = world.getWorldBorder();
        double originalSize = worldBorder.getSize();

        // Set a temporary smaller border size
        worldBorder.setSize(1);
        
        // Teleport players out of the chunk
        for (Player player : world.getPlayers()) {
            if (player.getLocation().getChunk().equals(chunk)) {
                player.teleport(world.getSpawnLocation());
            }
        }

        // Regenerate the chunk
        chunk.load();
        chunk.unload(true);

        // Restore the original world border size
        worldBorder.setSize(originalSize);
        
        getLogger().info("Chunk at X:" + chunkX + ", Z:" + chunkZ + " in world " + world.getName() + " has been regenerated.");
    }

public class PlayerTimeListener implements Listener {

    private final Map<UUID, ChunkCoordinates> playerChunkMap = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        ChunkCoordinates previousChunk = playerChunkMap.get(playerUUID);
        Chunk currentChunk = player.getLocation().getChunk();

        if (previousChunk == null || !previousChunk.equals(currentChunk)) {
            long timeDifference = System.currentTimeMillis() - (previousChunk != null ? previousChunk.getTimestamp() : 0);
            playerChunkMap.put(playerUUID, new ChunkCoordinates(currentChunk, System.currentTimeMillis()));
            // Do something with the time difference, such as adding it to a total time counter
        }
    }

    private static class ChunkCoordinates {
        private final int x;
        private final int z;
        private final long timestamp;

        public ChunkCoordinates(Chunk chunk, long timestamp) {
            this.x = chunk.getX();
            this.z = chunk.getZ();
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkCoordinates that = (ChunkCoordinates) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    public class PlayerTimeListener implements Listener {

        private final Map<UUID, ChunkCoordinates> playerChunkMap = new HashMap<>();
    
        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            ChunkCoordinates previousChunk = playerChunkMap.get(playerUUID);
            Chunk currentChunk = player.getLocation().getChunk();
    
            if (previousChunk == null || !previousChunk.equals(currentChunk)) {
                long timeDifference = System.currentTimeMillis() - (previousChunk != null ? previousChunk.getTimestamp() : 0);
                playerChunkMap.put(playerUUID, new ChunkCoordinates(currentChunk, System.currentTimeMillis()));
                // Do something with the time difference, such as adding it to a total time counter
            }
        }
    
        private static class ChunkCoordinates {
            private final int x;
            private final int z;
            private final long timestamp;
    
            public ChunkCoordinates(Chunk chunk, long timestamp) {
                this.x = chunk.getX();
                this.z = chunk.getZ();
                this.timestamp = timestamp;
            }
    
            public long getTimestamp() {
                return timestamp;
            }
    
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ChunkCoordinates that = (ChunkCoordinates) o;
                return x == that.x && z == that.z;
            }
    
            @Override
            public int hashCode() {
                return Objects.hash(x, z);
            }
        }
    }
}