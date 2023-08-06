package com.stemcraft.feature;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public class SMConsolePlayerDeathLoc extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getEventManager().registerEvent(PlayerDeathEvent.class, (listener, rawEvent) -> {
            if(rawEvent.getEventName().equalsIgnoreCase("playerdeathevent")) {
                PlayerDeathEvent event = (PlayerDeathEvent)rawEvent;
                
                Player player = event.getEntity();
                Location deathLocation = player.getLocation();
        
                String worldName = deathLocation.getWorld().getName();
                int x = deathLocation.getBlockX();
                int y = deathLocation.getBlockY();
                int z = deathLocation.getBlockZ();
            
                this.plugin.getLogger().info(player.getName() + " died at " + x + ", " + y + ", " + z + ", " + worldName);
            }
        });

        return true;
    }
}
