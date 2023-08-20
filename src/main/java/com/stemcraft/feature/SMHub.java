package com.stemcraft.feature;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class SMHub extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("HUB_TELEPORTED", "&eTeleported back to the hub");

        String[] aliases = new String[]{};

        // Hub Command
        this.plugin.getCommandManager().registerCommand("hub", (sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                return true;
            }

            if (!sender.hasPermission("stemcraft.hub")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            Player player = (Player)sender;
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());

            return true;
        }, aliases);

        // Player Join Event
        this.plugin.getEventManager().registerEvent(PlayerJoinEvent.class, (listener, rawEvent) -> {
            PlayerJoinEvent event = (PlayerJoinEvent)rawEvent;
            Player player = event.getPlayer();

            if(!player.hasPermission("stemcraft.hub.override")) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        });

        return true;
    }
}
