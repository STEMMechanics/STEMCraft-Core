package com.stemcraft.feature;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Shulker;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class SMVanillaSpawnChanges extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getEventManager().registerEvent(EntitySpawnEvent.class, (listener, rawEvent) -> {
            EntitySpawnEvent event = (EntitySpawnEvent)rawEvent;
            
            if(this.onEntitySpawnHelper(event.getEntityType(), event.getLocation()) == true) {
                event.setCancelled(true);
            }
        });

        this.plugin.getEventManager().registerEvent(ChunkLoadEvent.class, (listener, rawEvent) -> {
            ChunkLoadEvent event = (ChunkLoadEvent)rawEvent;
            
            for (org.bukkit.entity.Entity entity : event.getChunk().getEntities()) {
                if(this.onEntitySpawnHelper(entity.getType(), entity.getLocation()) == true) {
                    entity.remove();
                }
            }
        });

        return true;
    }

    private Boolean onEntitySpawnHelper(EntityType entityType, Location spawnLocation) {
        // Dont spawn phantoms outside of THE_END
        if (entityType == EntityType.PHANTOM &&
            spawnLocation.getWorld().getEnvironment() != Environment.THE_END) {
            return true;
        }

        // Target Enderman in THE_END
        if (entityType == EntityType.ENDERMAN &&
            spawnLocation.getWorld().getEnvironment() == Environment.THE_END) {
                
                // 50% chance of the Enderman being a shulkbox instead if:
                // the biome is highlands or midlands, the spawn is on a
                // purpur block and there are no other shulkers with 10 blocks
                Biome biome = spawnLocation.getBlock().getBiome();
                if (biome == Biome.END_HIGHLANDS || biome == Biome.END_MIDLANDS) {
            
                    // Check if there are no Shulkers within a radius of 10 blocks
                    boolean shulkerNearby = false;
                    for (Shulker shulker : spawnLocation.getWorld().getEntitiesByClass(Shulker.class)) {
                        if (shulker.getLocation().distance(spawnLocation) <= 10) {
                            shulkerNearby = true;
                            break;
                        }
                    }
                    
                    if (!shulkerNearby) {
                        if (Math.random() < 0.25) {
                            spawnLocation.getWorld().spawn(spawnLocation, Shulker.class);
                            return true;
                        }
                    }
                }

                // 50% chance of cancelling the Enderman spawn and spawning a Phantom
                if (Math.random() < 0.25) {
                    spawnLocation.getWorld().spawn(spawnLocation, Phantom.class);
                    return true;
                }
        }

        return false;
    }
}
