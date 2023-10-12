package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;

public class SMNightVision extends SMFeature {
    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        new SMCommand("nightvision")
            .alias("nv")
            .permission("stemcraft.nightvision")
            .action(ctx -> {
                // if (!(sender instanceof Player)) {
                //     this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                //     return true;
                // }

                toggleNightVision((Player)ctx.sender);
            })
            .register();

        return true;
    }

    private void enableNightVision(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        SMMessenger.infoLocale(player, "NIGHTVISION_ENABLED");
    }

    private void disableNightVision(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        SMMessenger.infoLocale(player, "NIGHTVISION_DISABLED");
    }

    private void toggleNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            disableNightVision(player);
        } else {
            enableNightVision(player);
        }
    }
}
