package com.stemcraft.feature;

import org.bukkit.ChatColor;
import org.bukkit.event.server.ServerListPingEvent;

public class SMMOTD extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getEventManager().registerEvent(ServerListPingEvent.class, (listener, rawEvent) -> {
            ServerListPingEvent event = (ServerListPingEvent)rawEvent;

            String motd = "&6Now on 1.20.1";

            if(this.plugin.getDatabaseManager().getMeta("maintenance", false)) {
                motd = "&6Server under maintenance";
            }
    
            event.setMotd(ChatColor.translateAlternateColorCodes('&', motd));
        });

        return true;
    }
}
