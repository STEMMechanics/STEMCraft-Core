package com.stemcraft.listener.entity;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.stemcraft.language.Phrase;
import com.stemcraft.utility.Util;

public class EntityDeathListener implements Listener {
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        // Drop mob head if killed by a player
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            if (player.getGameMode() == GameMode.SURVIVAL) {
                EntityType entityType = event.getEntityType();

                if (entityType != EntityType.PLAYER) {
                    ItemStack mobHead = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) mobHead.getItemMeta();
                    skullMeta.setOwner("MHF_" + entityType.toString());
                    skullMeta.setDisplayName(Util.capitalize(entityType.getKey().getKey() + " " + Phrase.build(Phrase.HEAD), true));
                    mobHead.setItemMeta(skullMeta);

                    event.getDrops().add(mobHead);
                }
            }
        }
    }

}
