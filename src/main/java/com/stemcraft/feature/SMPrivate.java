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

public class SMPrivate extends SMFeature {
    private Set<UUID> blockedPlayers = new HashSet<>();
    private Set<String> interactNonce = new HashSet<>();

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("PRIVATE_CORRECT", ":info_blue: &bPrivate password correct!");
        this.plugin.getLanguageManager().registerPhrase("PRIVATE_INCORRECT", ":warning_red: &cIncorrect password");
        this.plugin.getLanguageManager().registerPhrase("PRIVATE_REQUIRED_CHAT", "&e:warning_yellow: &eThe server in private mode.\n \n    &fYou are required to enter the password in chat to\n    stay connected");
        this.plugin.getLanguageManager().registerPhrase("PRIVATE_REQUIRED_KICK", "&cYou did not enter the login password");
        this.plugin.getLanguageManager().registerPhrase("PRIVATE_REQUIRED", ":warning_red: &cEnter the password before interacting");

        this.plugin.getConfigManager().getConfig().registerValue("private-password", "", "Password required to play on this server");

        // Player Join Event
        this.plugin.getEventManager().registerEvent(PlayerJoinEvent.class, (listener, rawEvent) -> {
            PlayerJoinEvent event = (PlayerJoinEvent)rawEvent;
            Player player = event.getPlayer();

            if(!player.hasPermission("stemcraft.private")) {
                String password = this.plugin.getConfigManager().getConfig().getValue("private-password");
                if(password.length() > 0) {
                    this.blockedPlayers.add(player.getUniqueId());

                    this.plugin.delayedTask(20 * 20L, (data) -> {
                        if (blockedPlayers.contains(player.getUniqueId())) {
                            if(this.plugin.getServer().getOnlinePlayers().contains(player)) {
                                player.kickPlayer(this.plugin.getLanguageManager().getPhrase("PRIVATE_REQUIRED_KICK"));
                            }
                        }
                    }, null);

                    this.plugin.delayedTask(20L, (data) -> {
                        this.plugin.getLanguageManager().sendBlank(player);
                        this.plugin.getLanguageManager().sendSeperator(player, ChatColor.YELLOW);
                        this.plugin.getLanguageManager().sendPhrase(player, "PRIVATE_REQUIRED_CHAT");
                        this.plugin.getLanguageManager().sendSeperator(player, ChatColor.YELLOW);
                        this.plugin.getLanguageManager().sendBlank(player);
                    }, null);
                }
            }
        });

        // Player Chat Event
        this.plugin.getEventManager().registerEvent(AsyncPlayerChatEvent.class, EventPriority.HIGHEST, (listener, rawEvent) -> {
            AsyncPlayerChatEvent event = (AsyncPlayerChatEvent)rawEvent;
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            if(this.blockedPlayers.contains(playerId)) {
                String password = this.plugin.getConfigManager().getConfig().getValue("private-password");
                if(password.length() > 0) {
                    event.setCancelled(true);

                    String message = event.getMessage().toLowerCase();
                    if (!message.equalsIgnoreCase(password)) {
                        this.plugin.getLanguageManager().sendPhrase(player, "PRIVATE_INCORRECT");
                        return;
                    }

                    this.plugin.getLanguageManager().sendPhrase(player, "PRIVATE_CORRECT");
                }

                this.blockedPlayers.remove(playerId);
            }
        });

        // Player Command Event
        this.plugin.getEventManager().registerEvent(PlayerCommandPreprocessEvent.class, EventPriority.HIGH, (listener, rawEvent) -> {
            PlayerCommandPreprocessEvent event = (PlayerCommandPreprocessEvent)rawEvent;

            if(this.checkCancelEvent(event.getPlayer())) {
                event.setCancelled(true);
            }
        });

        // Player Interact Event
        this.plugin.getEventManager().registerEvent(PlayerInteractEvent.class, EventPriority.HIGH, (listener, rawEvent) -> {
            PlayerInteractEvent event = (PlayerInteractEvent)rawEvent;
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock == null) {
                return;
            }

            if (!interactNonce.contains(player.getName())) {
                interactNonce.add(player.getName());

                this.plugin.delayedTask(5L, (data) -> {
                    interactNonce.remove(player.getName());
                }, null);

                if(this.checkCancelEvent(event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
        });

        // Block Break Event
        this.plugin.getEventManager().registerEvent(BlockBreakEvent.class, EventPriority.HIGH, (listener, rawEvent) -> {
            BlockBreakEvent event = (BlockBreakEvent)rawEvent;

            if(this.checkCancelEvent(event.getPlayer())) {
                event.setCancelled(true);
            }
        });

        // Block Place Event
        this.plugin.getEventManager().registerEvent(BlockPlaceEvent.class, EventPriority.HIGH, (listener, rawEvent) -> {
            BlockPlaceEvent event = (BlockPlaceEvent)rawEvent;

            if(this.checkCancelEvent(event.getPlayer())) {
                event.setCancelled(true);
            }
        });

        // Inventory Open Event
        this.plugin.getEventManager().registerEvent(InventoryOpenEvent.class, EventPriority.HIGH, (listener, rawEvent) -> {
            InventoryOpenEvent event = (InventoryOpenEvent)rawEvent;

            if(this.checkCancelEvent((Player)event.getPlayer())) {
                event.setCancelled(true);
            }
        });

        return true;
    }

    private Boolean checkCancelEvent(Player player) {
        if(player != null) {
            UUID playerId = player.getUniqueId();

            if(this.blockedPlayers.contains(playerId)) {
                this.plugin.getLanguageManager().sendPhrase(player, "PRIVATE_REQUIRED");
                return true;
            }
        }

        return false;
    }
}
