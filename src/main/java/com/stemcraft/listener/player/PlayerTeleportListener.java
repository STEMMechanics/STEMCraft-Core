package com.stemcraft.listener.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.stemcraft.component.ComponentTeleportBack;

public class PlayerTeleportListener implements Listener{
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        ComponentTeleportBack.playerPreviousLocations.put(player.getUniqueId(), event.getFrom());
    }
}
