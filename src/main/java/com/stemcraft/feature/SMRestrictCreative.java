package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.PortalCreateEvent;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.event.SMEvent;

public class SMRestrictCreative extends SMFeature {
    private final String permission = "stemcraft.creative.override";

    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerInteractEntityEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE
                && !ctx.event.getPlayer().hasPermission(this.permission)) {
                SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_INTERACT");
                ctx.event.setCancelled(true);
            }
        });

        SMEvent.register(InventoryClickEvent.class, ctx -> {
            if (ctx.event.getWhoClicked() instanceof Player player) {
                if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission(permission)) {
                    if (!ctx.event.getView().getTitle().equals(player.getOpenInventory().getTitle())) {
                        ctx.event.setCancelled(true);
                    }
                }
            }
        });

        SMEvent.register(EntityDeathEvent.class, ctx -> {
            if (ctx.event.getEntity() instanceof Player player) {
                if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission(permission)) {
                    ctx.event.getDrops().clear();
                }
            }
        });

        SMEvent.register(PlayerDropItemEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission(permission)) {
                SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_DROPS");
                ctx.event.setCancelled(true);
            }
        });

        SMEvent.register(EntityPickupItemEvent.class, ctx -> {
            if (ctx.event.getEntity() instanceof Player player) {
                if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission(permission)) {
                    SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_PICKUPS");
                    ctx.event.setCancelled(true);
                }
            }
        });

        SMEvent.register(PortalCreateEvent.class, ctx -> {
            if (ctx.event.getEntity() instanceof Player player) {
                if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission(permission)) {
                    SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_PORTALS");
                    ctx.event.setCancelled(true);
                }
            }
        });

        SMEvent.register(BlockPlaceEvent.class, ctx -> {
            Player player = (Player) ctx.event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission(permission)) {
                Material blockType = ctx.event.getBlockPlaced().getType();
                if (blockType == Material.END_PORTAL_FRAME) {
                    SMMessenger.errorLocale(player, "RESTRICT_CREATIVE_NO_PLACE");
                    ctx.event.setCancelled(true);
                }
            }
        });

        return true;
    }
}
