package com.stemcraft.feature;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SMGameMode extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("GAMEMODE_UNKNOWN", "Gamemode unknown");
        this.plugin.getLanguageManager().registerPhrase("GAMEMODE_CHANGED", "Gamemode set to %GAMEMODE%");
        this.plugin.getLanguageManager().registerPhrase("GAMEMODE_CHANGED_FOR", "Gamemode for %PLAYER_NAME% set to %GAMEMODE%");
        this.plugin.getLanguageManager().registerPhrase("GAMEMODE_CHANGED_BY", "Gamemode set to %GAMEMODE% by %PLAYER_NAME%");

        String[] aliases = new String[]{"gmc", "gms", "gmsp"};
        String[][] tabCompletions = new String[][]{
            {"gma", "%player%"},
            {"gmc", "%player%"},
            {"gms", "%player%"},
            {"gmsp", "%player%"},
        };

        this.plugin.getCommandManager().registerCommand("gma", (sender, command, label, args) -> {
            updateGameMode(sender, command, label, args);
            return true;
        }, aliases, tabCompletions);

        return true;
    }

    public void updateGameMode(CommandSender sender, Command command, String label, String[] args) {
        Player targetPlayer = null;
        String gamemodeStr = "Unknown";

        if(args.length < 1) {
            if (sender instanceof Player) {
                targetPlayer = (Player) sender;
            } else {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_REQ_FROM_CONSOLE");
                return;
            }
        } else {
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                return;
            }
        }
    
        if (label.equalsIgnoreCase("gma")) {
            targetPlayer.setGameMode(GameMode.ADVENTURE);
            gamemodeStr = "Adventure";
        } else if (label.equalsIgnoreCase("gmc")) {
            targetPlayer.setGameMode(GameMode.CREATIVE);
            gamemodeStr = "Creative";
        } else if (label.equalsIgnoreCase("gms")) {
            targetPlayer.setGameMode(GameMode.SURVIVAL);
            gamemodeStr = "Survival";
        } else if (label.equalsIgnoreCase("gmsp")) {
            targetPlayer.setGameMode(GameMode.SPECTATOR);
            gamemodeStr = "Spectator";
        } else {
            this.plugin.getLanguageManager().sendPhrase(sender, "GAMEMODE_UNKNOWN");
            return;
        }

        if(targetPlayer == sender) {
            HashMap<String, String> replacements = new HashMap<>();
                    
            replacements.put("GAMEMODE", gamemodeStr);
            this.plugin.getLanguageManager().sendPhrase(sender, "GAMEMODE_CHANGED", replacements);
        } else {
            HashMap<String, String> replacements = new HashMap<>();
                    
            replacements.put("PLAYER_NAME", targetPlayer.getName());
            replacements.put("GAMEMODE", gamemodeStr);
            this.plugin.getLanguageManager().sendPhrase(sender, "GAMEMODE_CHANGED_FOR", replacements);

            replacements.put("PLAYER_NAME", sender.getName());
            this.plugin.getLanguageManager().sendPhrase(sender, "GAMEMODE_CHANGED_BY", replacements);
        }
    }
}
