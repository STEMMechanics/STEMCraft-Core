package com.stemcraft.feature;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SMSeen extends SMFeature {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
    private String permission = "stemcraft.seen";

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("SEEN_USAGE", "Usage: /seen <player>");
        this.plugin.getLanguageManager().registerPhrase("SEEN_RESULT", "&e%PLAYER_NAME% last loggin in on %LAST_LOGIN%");

        String[] aliases = new String[]{};

        // Seen Command
        this.plugin.getCommandManager().registerCommand("seen", (sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                return true;
            }

            if (!sender.hasPermission(permission)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            if(args.length < 1) {
                this.plugin.getLanguageManager().sendPhrase(sender, "SEEN_USAGE");
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                return true;
            }

            Date lastLogin = new Date(targetPlayer.getLastPlayed());

            HashMap<String, String> replacements = new HashMap<>();
            replacements.put("PLAYER_NAME", targetPlayer.getName());
            replacements.put("LAST_SEEN", dateFormat.format(lastLogin));
            
            this.plugin.getLanguageManager().sendPhrase(sender, "SEEN_RESULT", replacements);

            return true;
        }, aliases);

        return true;
    }
}
