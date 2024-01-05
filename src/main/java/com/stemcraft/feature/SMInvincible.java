package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

/**
 * Allows players to enable/disable night vision via command.
 */
public class SMInvincible extends SMFeature {

    /**
     * Enables the night vision feature by registering the command.
     *
     * @return true if the feature is successfully enabled, otherwise false.
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerJoinEvent.class, ctx -> {
            setInvincible(ctx.event.getPlayer());
        });

        SMEvent.register(PlayerGameModeChangeEvent.class, ctx -> {
            setInvincible(ctx.event.getPlayer());
        });

        SMEvent.register(PlayerTeleportEvent.class, ctx -> {
            setInvincible(ctx.event.getPlayer());
        });

        return true;
    }

    public void setInvincible(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 255));
    }
}
