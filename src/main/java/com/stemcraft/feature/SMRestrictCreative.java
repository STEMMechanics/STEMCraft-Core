package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class SMRestrictCreative extends SMFeature {
    private String permission = "stemcraft.creative.override";

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("RESTRICT_CREATIVE_NO_DROPS", "&e:warning_yellow: &eYou cannot drop items in creative mode.");
        this.plugin.getLanguageManager().registerPhrase("RESTRICT_CREATIVE_NO_PICKUPS", "&e:warning_yellow: &eYou cannot pick up items in creative mode.");
        this.plugin.getLanguageManager().registerPhrase("RESTRICT_CREATIVE_NO_INTERACT", "&e:warning_yellow: &eYou cannot interact with that item in creative mode.");

        this.plugin.getEventManager().registerEvent(PlayerInteractEntityEvent.class, (listener, rawEvent) -> {
            PlayerInteractEntityEvent event = (PlayerInteractEntityEvent) rawEvent;
            
            Player player = event.getPlayer();
            if(player.getGameMode() == GameMode.CREATIVE && event.getPlayer().hasPermission(this.permission) == false) {
                this.plugin.getLanguageManager().sendPhrase(player, "RESTRICT_CREATIVE_NO_INTERACT");
                event.setCancelled(true);
            }
        });

        this.plugin.getEventManager().registerEvent(InventoryClickEvent.class, (listener, rawEvent) -> {
            InventoryClickEvent event = (InventoryClickEvent) rawEvent;
            
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(this.permission) == false) {
                    if (event.getView().getTitle().equals(player.getOpenInventory().getTitle()) == false) {
                        event.setCancelled(true);
                    }
                }
            }
        });

        this.plugin.getEventManager().registerEvent(EntityDeathEvent.class, (listener, rawEvent) -> {
            EntityDeathEvent event = (EntityDeathEvent) rawEvent;
            
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(this.permission) == false) {
                    event.getDrops().clear();
                }
            }
        });

        this.plugin.getEventManager().registerEvent(PlayerDropItemEvent.class, (listener, rawEvent) -> {
            PlayerDropItemEvent event = (PlayerDropItemEvent) rawEvent;
            
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(this.permission) == false) {
                this.plugin.getLanguageManager().sendPhrase(player, "RESTRICT_CREATIVE_NO_DROPS");
                event.setCancelled(true);
            }
        });

        this.plugin.getEventManager().registerEvent(EntityPickupItemEvent.class, (listener, rawEvent) -> {
            EntityPickupItemEvent event = (EntityPickupItemEvent) rawEvent;
            
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(this.permission) == false) {
                    this.plugin.getLanguageManager().sendPhrase(player, "RESTRICT_CREATIVE_NO_PICKUPS");
                    event.setCancelled(true);
                }
            }
        });
    
        return true;
    }
}
