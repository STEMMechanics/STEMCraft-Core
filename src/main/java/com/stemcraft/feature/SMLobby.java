package com.stemcraft.feature;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SMLobby extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("LOBBY_TELEPORTED", "&eTeleported back to lobby");

        String[] aliases = new String[]{"hub", "tplobby", "tphub"};

        this.plugin.getCommandManager().registerCommand("lobby", (sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                return true;
            }

            if (!sender.hasPermission("stemcraft.lobby")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            Player player = (Player)sender;
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());

            return true;
        }, aliases);

        return true;
    }
}
