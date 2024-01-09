package com.stemcraft.feature;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

/**
 * Allows players to enable/disable night vision via command.
 */
public class SMInvincible extends SMFeature {
    private final Set<UUID> invinciblePlayers = new HashSet<>();

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

        SMEvent.register(PlayerGameModeChangeEvent.class, EventPriority.HIGHEST, ctx -> {
            clearInvincible(ctx.event.getPlayer());
        });

        SMEvent.register(PlayerGameModeChangeEvent.class, EventPriority.LOWEST, ctx -> {
            STEMCraft.runLater(1, () -> {
                setInvincible(ctx.event.getPlayer());
            });
        });

        SMEvent.register(PlayerTeleportEvent.class, EventPriority.HIGHEST, ctx -> {
            clearInvincible(ctx.event.getPlayer());
        });

        SMEvent.register(PlayerTeleportEvent.class, EventPriority.LOWEST, ctx -> {
            STEMCraft.runLater(1, () -> {
                setInvincible(ctx.event.getPlayer());
            });
        });

        return true;
    }

    public void clearInvincible(Player player) {
        UUID uuid = player.getUniqueId();
        if (invinciblePlayers.contains(uuid)) {
            invinciblePlayers.remove(uuid);
            STEMCraft.cancelRunOnceDelay("invincible_" + uuid.toString());
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        }
    }

    public void setInvincible(Player player) {
        UUID uuid = player.getUniqueId();
        invinciblePlayers.add(uuid);
        STEMCraft.runOnceDelay("invincible_" + uuid.toString(), 400, () -> {
            invinciblePlayers.remove(uuid);
        });
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 255, true, false));
    }
}
