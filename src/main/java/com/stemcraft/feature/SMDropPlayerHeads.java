package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.stemcraft.core.SMDebugger;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

public class SMDropPlayerHeads extends SMFeature {

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerDeathEvent.class, ctx -> {
            if(ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
                Player player = ctx.event.getEntity();
                Player killer = player.getKiller();

                // Check if the killer is a player and not in survival gamemode
                if (killer instanceof Player && (killer.getUniqueId() == player.getUniqueId() || killer.getGameMode() != GameMode.SURVIVAL)) {
                    SMDebugger.debug(this, "Player not killed by another player that was in survival");
                    return;
                }

                if (player.getGameMode() == GameMode.SURVIVAL) {
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                    skullMeta.setOwningPlayer(player);
                    playerHead.setItemMeta(skullMeta);

                    ctx.event.getDrops().add(playerHead);
                    SMDebugger.debug(this, "Player killed by another player. Added head to drop list");
                } else {
                    SMDebugger.debug(this, "Player killed by another player but not in survival game mode");
                }
            }
        });

        return true;
    }
}
