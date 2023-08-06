package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SMDropPlayerHeads extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getEventManager().registerEvent(PlayerDeathEvent.class, (listener, rawEvent) -> {
            if(rawEvent.getEventName().equalsIgnoreCase("playerdeathevent")) {
                PlayerDeathEvent event = (PlayerDeathEvent)rawEvent;
                Player player = event.getEntity();
                Player killer = player.getKiller();

                // Check if the killer is a player and not in survival gamemode
                if (killer instanceof Player && killer.getGameMode() != GameMode.SURVIVAL) {
                    return;
                }

                if (player.getGameMode() == GameMode.SURVIVAL) {
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                    skullMeta.setOwningPlayer(player);
                    playerHead.setItemMeta(skullMeta);

                    event.getDrops().add(playerHead);
                    event.getDrops().add(new ItemStack(Material.IRON_AXE));
                }
            }
        });

        return true;
    }
}
