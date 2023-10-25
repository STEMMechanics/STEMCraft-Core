package com.stemcraft.feature;

import org.bukkit.entity.Enderman;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

public class SMNoEndermanPickups extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(EntityChangeBlockEvent.class, ctx -> {
            if(ctx.event.getEntity() instanceof Enderman) {
                ctx.event.setCancelled(true);
            }
        });

        return true;
    }
}
