package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class SMSpawnEggs extends SMFeature {
    private String permission = "stemcraft.spawneggs";

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("SPAWNEGGS_DENIED", ":warning_red: &cYou do not have permission to use spawn eggs");

        this.plugin.getEventManager().registerEvent(PlayerInteractEvent.class, (listener, rawEvent) -> {
            PlayerInteractEvent event = (PlayerInteractEvent)rawEvent;
            Player player = event.getPlayer();
            
            if(player.hasPermission(this.permission) == false) {
                this.plugin.getLanguageManager().sendPhrase(player, "SPAWNEGGS_DENIED");
                event.setCancelled(true);
            }
        });

        return true;
    }
}
