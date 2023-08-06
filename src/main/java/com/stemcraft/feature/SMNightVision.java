package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SMNightVision extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("NIGHTVISION_ENABLED", "&eNight vision enabled");
        this.plugin.getLanguageManager().registerPhrase("NIGHTVISION_DISABLED", "&eNight vision disabled");

        String[] aliases = new String[]{"nv"};

        this.plugin.getCommandManager().registerCommand("nightvision", (sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                return true;
            }

            if (!sender.hasPermission("stemcraft.nightvision")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            toggleNightVision((Player)sender);
            return true;
        }, aliases);

        return true;
    }

    private void enableNightVision(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        this.plugin.getLanguageManager().sendPhrase(player, "NIGHTVISION_ENABLED");
    }

    private void disableNightVision(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        this.plugin.getLanguageManager().sendPhrase(player, "NIGHTVISION_DISABLED");
    }

    private void toggleNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            disableNightVision(player);
        } else {
            enableNightVision(player);
        }
    }
}
