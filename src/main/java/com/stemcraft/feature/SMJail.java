package com.stemcraft.feature;

import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.SMPersistent;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.config.SMConfigFile;
import com.stemcraft.core.event.SMEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Set the player chat formatting
 */
public class SMJail extends SMFeature {
    Location jailLocation = null;

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        jailLocation = SMPersistent.getObject(this, "jail_location", Location.class);

        /** Event Join */
        SMEvent.register(PlayerJoinEvent.class, (ctx) -> {
            PlayerJoinEvent event = (PlayerJoinEvent) ctx.event;
            Player player = event.getPlayer();

            if (isJailed(player)) {
                if (jailLocation != null) {
                    SMCommon.safePlayerTeleport(player, jailLocation);
                    player.setGameMode(GameMode.ADVENTURE);
                    SMMessenger.infoLocale(player, "JAILED");
                } else {
                    STEMCraft.warning("Could not jail " + player.getName() + " as no jail has been set");
                }
            }
        });

        /** Event Chat */
        SMEvent.register(AsyncPlayerChatEvent.class, ctx -> {
            if (isJailed(ctx.event.getPlayer())) {
                ctx.event.setCancelled(true);
                SMMessenger.errorLocale(ctx.event.getPlayer(), "JAILED_NO_CHAT");
            }
        });

        /** Event Command Preprocess */
        SMEvent.register(PlayerCommandPreprocessEvent.class, ctx -> {
            if (isJailed(ctx.event.getPlayer())) {
                ctx.event.setCancelled(true);
                SMMessenger.errorLocale(ctx.event.getPlayer(), "JAILED_NO_COMMAND");
            }
        });

        /** Event Teleport */
        SMEvent.register(PlayerTeleportEvent.class, ctx -> {
            if (isJailed(ctx.event.getPlayer())) {
                ctx.event.setCancelled(true);
            }
        });

        /** Event Teleport */
        SMEvent.register(PlayerRespawnEvent.class, ctx -> {
            if (isJailed(ctx.event.getPlayer())) {
                if (isJailSet()) {
                    ctx.event.setRespawnLocation(jailLocation);
                }
            }
        });

        /** Jail player */
        new SMCommand("jail")
            .tabComplete("{player}")
            .permission("stemcraft.command.jail")
            .action(ctx -> {
                ctx.checkArgs(1, SMLocale.get("JAILED_USAGE"));

                Player targetPlayer = ctx.getArgAsPlayer(1, null);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                if (!isJailed(targetPlayer)) {
                    if (isJailSet()) {
                        jailPlayer(targetPlayer);
                        SMMessenger.infoLocale(targetPlayer, "JAILED");
                        ctx.returnInfoLocale("JAILED_SUCCESS", "player", targetPlayer.getName());
                    } else {
                        ctx.returnErrorLocale("JAILED_NOT_SET");
                    }
                } else {
                    ctx.returnErrorLocale("JAILED_ALREADY");
                }
            })
            .register();

        /** Unjail player */
        new SMCommand("unjail")
            .tabComplete("{player}")
            .permission("stemcraft.command.jail")
            .action(ctx -> {
                ctx.checkArgs(1, SMLocale.get("UNJAILED_USAGE"));

                Player targetPlayer = ctx.getArgAsPlayer(1, null);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                if (isJailed(targetPlayer)) {
                    unjailPlayer(targetPlayer);
                    SMMessenger.infoLocale(targetPlayer, "UNJAILED");
                    ctx.returnInfoLocale("UNJAILED_SUCCESS", "player", targetPlayer.getName());
                } else {
                    ctx.returnErrorLocale("JAILED_NOT_UNJAILED");
                }
            })
            .register();

        /** Set Jail */
        new SMCommand("setjail")
            .permission("stemcraft.command.jail")
            .action(ctx -> {
                ctx.checkNotConsole();

                setJailLocation(ctx.player.getLocation());
                ctx.returnInfoLocale("JAILED_UPDATED");
            })
            .register();

        return true;
    }

    /**
     * Return if the player is currently jailed.
     * 
     * @param player The player to check.
     * @return Boolean value if the player is jailed.
     */
    public Boolean isJailed(Player player) {
        return SMPersistent.getObject(this, player.getUniqueId().toString() + "_jailed", Location.class) != null;
    }

    public Boolean isJailSet() {
        return jailLocation != null;
    }

    public void jailPlayer(Player player) {
        if (isJailSet()) {
            SMCommon.safePlayerTeleport(player, jailLocation);
            SMPersistent.set(this, player.getUniqueId().toString() + "_jailed", player.getLocation());
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    public void unjailPlayer(Player player) {
        Location prevLocation =
            SMPersistent.getObject(this, player.getUniqueId().toString() + "_jailed", Location.class);
        if (prevLocation != null) {
            SMPersistent.clear(this, player.getUniqueId().toString() + "_jailed");
            SMCommon.safePlayerTeleport(player, prevLocation);
        }
    }

    public void setJailLocation(Location location) {
        jailLocation = location;

        SMPersistent.set(this, "jail_location", location);

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (isJailed(player)) {
                SMCommon.safePlayerTeleport(player, jailLocation);
            }
        }
    }
}
