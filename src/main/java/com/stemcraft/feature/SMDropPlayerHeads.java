package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

public class SMDropPlayerHeads extends SMFeature {

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerDeathEvent.class, ctx -> {
            if (ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
                Player player = ctx.event.getEntity();
                Player killer = player.getKiller();

                if (killer instanceof Player) {
                    if (killer.getUniqueId().equals(player.getUniqueId())) {
                        return;
                    }

                    if (killer.getGameMode() != GameMode.SURVIVAL) {
                        return;
                    }

                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                        skullMeta.setOwningPlayer(player);
                        playerHead.setItemMeta(skullMeta);

                        ctx.event.getDrops().add(playerHead);
                    }
                }
            }
        });

        return true;
    }
}
