package com.stemcraft.listener.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.stemcraft.component.ComponentLockdown;
import com.stemcraft.utility.Meta;

public class PlayerCommandListener implements Listener {

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String lockdownCode = Meta.getString("lockdown", "").toLowerCase();
        if(lockdownCode.length() > 0) {
            Player player = event.getPlayer();
            if (ComponentLockdown.blockedPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.YELLOW + "You are required to enter the login code before you can run commands");
            }
        }
    }
}
