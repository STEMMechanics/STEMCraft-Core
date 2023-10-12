package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.event.SMEvent;

public class SMRestrictCreative extends SMFeature {
    private String permission = "stemcraft.creative.override";

    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerInteractEntityEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            if(player.getGameMode() == GameMode.CREATIVE && ctx.event.getPlayer().hasPermission(this.permission) == false) {
                SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_INTERACT");
                ctx.event.setCancelled(true);
            }
        });

        SMEvent.register(InventoryClickEvent.class, ctx -> {
            if (ctx.event.getWhoClicked() instanceof Player) {
                Player player = (Player) ctx.event.getWhoClicked();
                if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(permission) == false) {
                    if (ctx.event.getView().getTitle().equals(player.getOpenInventory().getTitle()) == false) {
                        ctx.event.setCancelled(true);
                    }
                }
            }
        });

        SMEvent.register(EntityDeathEvent.class, ctx -> {
            if (ctx.event.getEntity() instanceof Player) {
                Player player = (Player) ctx.event.getEntity();
                if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(permission) == false) {
                    ctx.event.getDrops().clear();
                }
            }
        });

        SMEvent.register(PlayerDropItemEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(permission) == false) {
                SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_DROPS");
                ctx.event.setCancelled(true);
            }
        });

        SMEvent.register(EntityPickupItemEvent.class, ctx -> {
            if (ctx.event.getEntity() instanceof Player) {
                Player player = (Player) ctx.event.getEntity();
                if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(permission) == false) {
                    SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_PICKUPS");
                    ctx.event.setCancelled(true);
                }
            }
        });
    
        return true;
    }
}
