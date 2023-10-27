package com.stemcraft.feature;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.SMPersistent;
import com.stemcraft.core.command.SMCommand;

/**
 * Allows players to enable/disable night vision via command.
 */
public class SMNightVision extends SMFeature {
    /**
     * If the SMPersistent feature is enabled to be used.
     */
    private static Boolean persistentEnabled = false;

    /**
     * Class constructor
     */
    public SMNightVision() {
        loadAfterFeatures.add("SMPersistent");
    }

    /**
     * Enables the night vision feature by registering the command.
     *
     * @return true if the feature is successfully enabled, otherwise false.
     */
    @Override
    protected Boolean onEnable() {
        if(STEMCraft.featureEnabled("SMPersistent")) {
            SMNightVision.persistentEnabled = true;
        }
        
        new SMCommand("nightvision")
            .alias("nv")
            .permission("stemcraft.command.nightvision")
            .tabComplete("enable", "{player}")
            .tabComplete("disable", "{player}")
            .tabComplete("toggle", "{player}")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(2, ctx.player);
                String action = "toggle";

                // get action (if exists)
                if(ctx.args.length > 0) {
                    action = ctx.args[0].toLowerCase();
                }

                // Check player exists when issued from console
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length == 2), "CMD_PLAYER_REQ_FROM_CONSOLE");

                // Check target player exists
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                if(action.equals("toggle")) {
                    SMNightVision.toggleNightVision(targetPlayer);
                } else if(action.equals("enable")) {
                    SMNightVision.enableNightVision(targetPlayer);
                } else if(action.equals("disable")) {
                    SMNightVision.disableNightVision(targetPlayer);
                } else {
                    ctx.returnErrorLocale("NIGHTVISION_USAGE");
                }
            })
            .register();

        return true;
    }

    /**
     * Enables night vision effect for the specified player. If the player already has
     * a night vision effect, it's saved before applying the new effect.
     *
     * @param player The player for whom the night vision effect is to be enabled.
     */
    public static void enableNightVision(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Save any current nightvision
        if (persistentEnabled && player.hasPotionEffect(PotionEffectType.NIGHT_VISION) && !SMPersistent.exists(SMNightVision.class, playerUUID.toString())) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType() == PotionEffectType.NIGHT_VISION) {
                    SMPersistent.set(SMNightVision.class, playerUUID.toString(), effect);
                    break;
                }
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        SMMessenger.infoLocale(player, "NIGHTVISION_ENABLED");
    }

    /**
     * Disables night vision effect for the specified player. If the player had a previously
     * saved night vision effect, it's restored after removing the current effect.
     *
     * @param player The player for whom the night vision effect is to be disabled.
     */
    public static void disableNightVision(Player player) {
        UUID playerUUID = player.getUniqueId();

        player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        // Restore any previous nightvision
        if(persistentEnabled && SMPersistent.exists(SMNightVision.class, playerUUID.toString())) {
            player.addPotionEffect(SMPersistent.getObject(SMNightVision.class, playerUUID.toString(), PotionEffect.class));
            SMPersistent.clear(SMNightVision.class, playerUUID.toString());
        }

        SMMessenger.infoLocale(player, "NIGHTVISION_DISABLED");
    }

    /**
     * Toggles the night vision effect for the specified player. If the player currently has
     * the effect, it's disabled, otherwise, it's enabled.
     *
     * @param player The player for whom the night vision effect is to be toggled.
     */
    public static void toggleNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            disableNightVision(player);
        } else {
            enableNightVision(player);
        }
    }

    /**
     * Clears the night vision effect for the specified player without restoring any 
     * previously saved effects.
     *
     * @param player The player for whom the night vision effect is to be cleared.
     */
    public static void clearNightVision(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            if(persistentEnabled) {
                SMPersistent.clear(SMNightVision.class, playerUUID.toString());
            }

            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
}
