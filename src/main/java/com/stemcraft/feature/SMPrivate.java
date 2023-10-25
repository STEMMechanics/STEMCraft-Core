package com.stemcraft.feature;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.event.SMEvent;

public class SMPrivate extends SMFeature {
    private Set<UUID> blockedPlayers = new HashSet<>();

    @Override
    protected Boolean onEnable() {
        // Player Join Event
        SMEvent.register(PlayerJoinEvent.class, ctx -> {
            if(!SMConfig.main().getBoolean("private-server.enabled")) {
                return;
            }

            Player player = ctx.event.getPlayer();
            if(!player.hasPermission("stemcraft.private")) {
                String password = SMConfig.main().getString("private-server.password");
                if(password.length() > 0) {
                    this.blockedPlayers.add(player.getUniqueId());

                    STEMCraft.runLater(20, () -> {
                        if (blockedPlayers.contains(player.getUniqueId())) {
                            if(STEMCraft.getPlugin().getServer().getOnlinePlayers().contains(player)) {
                                player.kickPlayer(SMLocale.get(player, "PRIVATE_REQUIRED_KICK"));
                            }
                        }
                    });

                    STEMCraft.runLater(20, () -> {
                        SMMessenger.blankLine(player);
                        SMMessenger.seperatorLine(player, ChatColor.YELLOW);
                        SMMessenger.warnLocale(player, "PRIVATE_REQUIRED_CHAT");
                        SMMessenger.seperatorLine(player, ChatColor.YELLOW);
                        SMMessenger.blankLine(player);
                    });
                }
            }
        });

        // Player Chat Event
        SMEvent.register(AsyncPlayerChatEvent.class, EventPriority.HIGHEST, ctx -> {
            Player player = ctx.event.getPlayer();
            UUID playerId = player.getUniqueId();

            if(this.blockedPlayers.contains(playerId)) {
                String password = SMConfig.main().getString("private-server.password");
                if(password.length() > 0) {
                    ctx.event.setCancelled(true);

                    String message = ctx.event.getMessage().toLowerCase();
                    if (!message.equalsIgnoreCase(password)) {
                        SMMessenger.errorLocale(player, "PRIVATE_INCORRECT");
                        return;
                    }

                    SMMessenger.successLocale(player, "PRIVATE_CORRECT");
                }

                this.blockedPlayers.remove(playerId);
            }
        });

        // Player Command Event
        SMEvent.register(PlayerCommandPreprocessEvent.class, EventPriority.HIGH, ctx -> {
            if(this.checkCancelEvent(ctx.event.getPlayer())) {
                ctx.event.setCancelled(true);
            }
        });

        // Player Interact Event
        SMEvent.register(PlayerInteractEvent.class, EventPriority.HIGH, ctx -> {
            Player player = ctx.event.getPlayer();
            Block clickedBlock = ctx.event.getClickedBlock();

            if (clickedBlock == null) {
                return;
            }

            STEMCraft.runOnce(player.getName(), 5L, () -> {
                if(checkCancelEvent(ctx.event.getPlayer())) {
                    ctx.event.setCancelled(true);
                }
            });
        });

        // Block Break Event
        SMEvent.register(BlockBreakEvent.class, EventPriority.HIGH, ctx -> {
            if(this.checkCancelEvent(ctx.event.getPlayer())) {
                ctx.event.setCancelled(true);
            }
        });

        // Block Place Event
        SMEvent.register(BlockPlaceEvent.class, EventPriority.HIGH, ctx -> {
            if(this.checkCancelEvent(ctx.event.getPlayer())) {
                ctx.event.setCancelled(true);
            }
        });

        // Inventory Open Event
        SMEvent.register(InventoryOpenEvent.class, EventPriority.HIGH, ctx -> {
            if(this.checkCancelEvent((Player)ctx.event.getPlayer())) {
                ctx.event.setCancelled(true);
            }
        });

        return true;
    }

    private Boolean checkCancelEvent(Player player) {
        if(player != null) {
            UUID playerId = player.getUniqueId();

            if(this.blockedPlayers.contains(playerId)) {
                SMMessenger.errorLocale(player, "PRIVATE_REQUIRED");
                return true;
            }
        }

        return false;
    }
}
