package com.stemcraft.feature;

import org.bukkit.entity.Player;

public class SMRules extends SMFeature {
    @Override
    protected Boolean onEnable() {
        // Rule Command
        this.plugin.getCommandManager().registerCommand("rules", (sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                return true;
            }

            SMBooks bookManager = (SMBooks)this.plugin.getFeatureManager().getFeature("SMBooks");
            bookManager.showBook((Player)sender, "server-rules");

            return true;
        });

        return true;
    }
}
