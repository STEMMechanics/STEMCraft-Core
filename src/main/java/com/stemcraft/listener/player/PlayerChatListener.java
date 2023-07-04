package com.stemcraft.listener.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.stemcraft.component.ComponentLockdown;
import com.stemcraft.utility.Meta;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String lockdownCode = Meta.getString("lockdown", "").toLowerCase();
        if(lockdownCode.length() > 0) {
            Player player = event.getPlayer();
            if (ComponentLockdown.blockedPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);

                String message = event.getMessage().toLowerCase();
                if (message.contains(lockdownCode)) {
                    ComponentLockdown.blockedPlayers.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "Thanks!");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Incorrect login code. Please try again");
                }
            }
        }
    }
}
