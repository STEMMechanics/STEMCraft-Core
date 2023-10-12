package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.event.SMEvent;

/**
 * Deny a player interacting with spawn eggs unless having the correct permission
 */
public class SMSpawnEggs extends SMFeature {
    private final static String PERMISSION = "stemcraft.spawneggs";

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerInteractEvent.class, ctx -> {
            PlayerInteractEvent event = (PlayerInteractEvent)ctx.event;
            Player player = event.getPlayer();
            
            if(player.getInventory().getItemInMainHand().getType().toString().endsWith("SPAWN_EGG")) {
                if(player.hasPermission(SMSpawnEggs.PERMISSION) == false) {
                    SMMessenger.error(player, SMLocale.get(player, "SPAWNEGGS_DENIED"));
                    event.setCancelled(true);
                }
            }
        });

        return true;
    }
}
