package com.stemcraft.feature;

import org.bukkit.entity.Enderman;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class SMNoEndermanPickups extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getEventManager().registerEvent(EntityChangeBlockEvent.class, (listener, rawEvent) -> {
            EntityChangeBlockEvent event = (EntityChangeBlockEvent)rawEvent;

            if(event.getEntity() instanceof Enderman) {
                event.setCancelled(true);
            }
        });

        return true;
    }
}
