package com.stemcraft.feature;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.SMReplacer;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.exception.SMException;

public class SMHub extends SMFeature {
    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        new SMCommand("hub")
            .alias("lobby")
            .permission("stemcraft.command.hub")
            .action(ctx -> {
                ctx.checkNotConsole();
                if(ctx.player != null) {
                    teleportToHub(ctx.player);
                }
            })
            .register();

        SMEvent.register(PlayerJoinEvent.class, (ctx) -> {
            PlayerJoinEvent event = (PlayerJoinEvent)ctx.event;
            Player player = event.getPlayer();

            if(!player.hasPermission("stemcraft.hub.override")) {
                teleportToHub(player);
            }
        });
        
        return true;
    }

    /**
     * Teleport player to hub world.
     * @param player
     */
    private final static void teleportToHub(Player player) {
        final String hubWorldName = SMConfig.main().getString("hub.world", "world");
        final String key = "hub.tp-commands." + player.getWorld().getName().toLowerCase();

        World hubWorld = Bukkit.getWorld(hubWorldName);
        if(hubWorld == null) {
            SMMessenger.error(player, SMLocale.get("HUB_NOT_DEFINED"));
            throw new SMException("Hub world " + hubWorldName + " not found"); 
        }

        if(SMConfig.main().contains(key)) {
            SMConfig.main().getStringList(key).forEach(command -> {
                SMReplacer.replaceVariables(command, "player", player.getName(), "hub-world", hubWorldName);
                Bukkit.getServer().dispatchCommand(player, command);
            });
        } else {
            player.teleport(hubWorld.getSpawnLocation());
        }

        SMMessenger.info(player, SMLocale.get("HUB_TELEPORTED"));
    }
}
