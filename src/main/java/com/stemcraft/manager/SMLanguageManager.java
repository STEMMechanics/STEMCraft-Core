package com.stemcraft.manager;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.feature.SMItemsAdder;
import me.clip.placeholderapi.PlaceholderAPI;

public class SMLanguageManager extends SMManager {
    private Map<String, String> phraseMap = new HashMap<>();
    SMItemsAdder iaFeature = null;

    @Override
    public void onEnable() {
        this.registerPhrase("CMD_NO_PERMISSION", ":warning_red: &cYou don't have permission to use this command");
        this.registerPhrase("CMD_ONLY_PLAYERS", "This command can only be run by a player");
        this.registerPhrase("CMD_PLAYER_REQ_FROM_CONSOLE", "A player name is required when run from console");
        this.registerPhrase("CMD_PLAYER_NOT_FOUND", ":warning_red: &cPlayer not found");
        this.registerPhrase("CMD_INVALID_OPTION", ":warning_red: &cUnknown option");
        this.registerPhrase("CMD_OPTION_REQUIRED", ":warning_red: &cAn option is required");

        this.iaFeature = (SMItemsAdder)this.plugin.getFeatureManager().getFeature("SMItemsAdder");
    }

    public void registerPhrase(String key, String phrase) {
        this.phraseMap.put(key, phrase);
    }
    
    public void registerPhraseMap(Map<String, String> map) {
        this.phraseMap.putAll(map);
    }

    public String getPhrase(String key) {
        return this.getPhrase(key, null, null);
    }

    public String getPhrase(String key, Player player) {
        return this.getPhrase(key, player, null);
    }

    public String getPhrase(String key, Map<String, String> replacements) {
        return this.getPhrase(key, null, replacements);
    }

    public String getPhrase(String key, Player player, Map<String, String> replacements) {
        String phrase = phraseMap.getOrDefault(key, "");

        if(this.iaFeature != null) {
            phrase = this.iaFeature.formatString(player, phrase);
        }
        
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String searchKey = "%" + entry.getKey() + "%";
                String replacement = entry.getValue();
                phrase = phrase.replace(searchKey, replacement);
            }
        }

        if(player != null) {
            phrase = PlaceholderAPI.setPlaceholders(player, phrase);
        }

        return ChatColor.translateAlternateColorCodes('&', phrase);
    }

    public void sendPhrase(CommandSender sender, String key) {
        this.sendPhrase(sender, key, null);
    }

    public void sendPhrase(CommandSender sender, String key, Map<String, String> replacements) {
        Player player = null;

        if(sender instanceof Player) {
            player = (Player)sender;
        }

        sender.sendMessage(this.getPhrase(key, player, replacements));
    }

    public void sendPhrase(Player player, String key) {
        this.sendPhrase(player, key, null);
    }

    public void sendPhrase(Player player, String key, Map<String, String> replacements) {
        player.sendMessage(this.getPhrase(key, player, replacements));
    }

    public void sendBlank(Player player) {
        player.sendMessage("   ");
    }

    public void sendSeperator(Player player, ChatColor color) {
        player.sendMessage(color + (ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80)));
    }

    public String parseString(String string) {
        if(this.iaFeature != null) {
            string = this.iaFeature.formatString(null, string);
        }

        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
