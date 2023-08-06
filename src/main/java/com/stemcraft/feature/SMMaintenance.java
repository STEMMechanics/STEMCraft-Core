package com.stemcraft.feature;

import java.util.HashMap;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class SMMaintenance extends SMFeature {
    private String permission = "stemcraft.maintenance";

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("MAINTENANCE_SET_TO", "Server maintenance set to %MODE%");
        this.plugin.getLanguageManager().registerPhrase("MAINTENANCE_KICK", "The server is currently undergoing maintenance");

        String[] aliases = new String[]{};
        String[][] tabCompletions = new String[][]{
            {"maintenance", "enable"},
            {"maintenance", "disable"},
        };

        this.plugin.getEventManager().registerEvent(PlayerLoginEvent.class, (listener, event) -> {
            PlayerLoginEvent e = (PlayerLoginEvent)event;

            if (this.plugin.getDatabaseManager().getMeta("maintenance", false) && !e.getPlayer().hasPermission(this.permission)) {
                // Maintenance mode is enabled and player does not have the permission
                e.setKickMessage(this.plugin.getLanguageManager().getPhrase("MAINTENANCE_KICK"));
                e.setResult(Result.KICK_OTHER);
            }
        });

        this.plugin.getCommandManager().registerCommand("maintenance", (sender, command, label, args) -> {
            if(!sender.hasPermission(this.permission)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            if(args.length > 1) {
                if(args[0].equalsIgnoreCase("enable")) {
                    this.plugin.getDatabaseManager().setMeta("maintenance", true);

                    this.plugin.getServer().getOnlinePlayers().forEach(player -> {
                        if (!player.hasPermission(this.permission)) {
                            player.kickPlayer(this.plugin.getLanguageManager().getPhrase("MAINTENANCE_KICK"));
                        }
                    });
                } else if(args[0].equalsIgnoreCase("disable")) {
                    this.plugin.getDatabaseManager().setMeta("maintenance", false);
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_INVALID_OPTION");
                    return true;
                }
            }
        
            HashMap<String, String> replacements = new HashMap<>();
            String modeString = "disabled";

            if(this.plugin.getDatabaseManager().getMeta("maintenance", false)) {
                modeString = "enabled";
            }

            replacements.put("MODE", modeString);
            this.plugin.getLanguageManager().sendPhrase(sender, "MAINTENANCE_SET_TO", replacements);

            return true;
        }, aliases, tabCompletions);

        return true;
    }
}
