package com.stemcraft.core.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import com.stemcraft.STEMCraft;

public class SMLoadChunk {
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
                    world.addPluginChunkTicket(chunkX, chunkZ, STEMCraft.getPlugin());
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

                        STEMCraft.severe("Failed to load and activate Chunk at X: " + (chunkX * 16) + " Z: " + (chunkZ * 16) + " in " + world.getName());
                    }
                }
            }
        }

        return success;
    }
}
