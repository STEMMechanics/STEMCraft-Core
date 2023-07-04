package com.stemcraft.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import com.stemcraft.utility.Meta;

public class PlayerLoginListener implements Listener{
    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event) {
        
        // Check if the maintenance mode is enabled
        if (Meta.getBoolean("maintenance", false) && !event.getPlayer().hasPermission("stemcraft.maintenance")) {
            
            // Maintenance mode is enabled and player does not have the permission
            String kickMessage = "The server is currently undergoing maintenance.";
            event.setKickMessage(kickMessage);
            event.setResult(Result.KICK_OTHER);
        }
    }
}
