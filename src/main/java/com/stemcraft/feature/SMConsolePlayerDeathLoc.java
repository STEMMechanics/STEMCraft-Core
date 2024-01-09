package com.stemcraft.feature;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

/**
 * Print the players death location in the console
 */
public class SMConsolePlayerDeathLoc extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerDeathEvent.class, ctx -> {
            if(ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
                Player player = ctx.event.getEntity();

                if(player.hasMetaData("NPC")) {
                    return;
                }

                Location deathLocation = player.getLocation();
        
                String worldName = deathLocation.getWorld().getName();
                int x = deathLocation.getBlockX();
                int y = deathLocation.getBlockY();
                int z = deathLocation.getBlockZ();
            
                STEMCraft.info(player.getName() + " died at " + x + ", " + y + ", " + z + ", " + worldName);
            }
        });

        return true;
    }
}
