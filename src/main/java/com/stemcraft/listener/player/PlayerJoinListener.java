package com.stemcraft.listener.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import com.stemcraft.STEMCraft;
import com.stemcraft.component.ComponentLockdown;
import com.stemcraft.utility.Meta;
import com.stemcraft.utility.Timer;

public class PlayerJoinListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(Meta.getString("lockdown", "").length() > 0) {
            Player player = event.getPlayer();
            ComponentLockdown.blockedPlayers.add(player.getUniqueId());

            Timer.start(20 * 20L, (data) -> {
                // Callback logic here, invoked when the timer expires
                // Use 'player' and 'data' as needed
                if (ComponentLockdown.blockedPlayers.contains(player.getUniqueId())) {
                    player.kickPlayer(ChatColor.RED + "You failed to type the login code within the given time. Goodbye!");
                }
            }, null);

            // new BukkitRunnable() {
            //     @Override
            //     public void run() {
            //         if (ComponentLockdown.blockedPlayers.contains(player.getUniqueId())) {
            //             player.kickPlayer(ChatColor.RED + "You failed to type the login code within the given time. Goodbye!");
            //         }
            //     }
            // }.runTaskLater(STEMCraft.getInstance(), 20 * 20L); // 20 seconds (20 ticks per second)

            player.sendMessage(ChatColor.YELLOW + "You are required to enter the login code before you can play on this server");
        }
    }
}
