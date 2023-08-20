package com.stemcraft.feature;

import org.bukkit.ChatColor;
import org.bukkit.event.server.ServerListPingEvent;

public class SMMOTD extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getEventManager().registerEvent(ServerListPingEvent.class, (listener, rawEvent) -> {
            ServerListPingEvent event = (ServerListPingEvent)rawEvent;

            String motdTitle = this.plugin.getConfigManager().getConfig().registerValue("motd-title", "&3■&b■&3■&b■&3■&b■&3■&b■&3■&b■&3■&b■&3■&b■&3■&b■&3■ &e&lSTEMCRAFT &3■&b■&3■&b■&3■&b■&3■&b■&3■&b■&3■&b■&3■&b■&3■&b■", "Server MOTD");
            String motdMessage = this.plugin.getConfigManager().getConfig().registerValue("motd-message", "", "");

            if(this.plugin.getDatabaseManager().getMeta("maintenance", false)) {
                motdTitle = "&6Server under maintenance";
                motdMessage = "";
            }

            String password = this.plugin.getConfigManager().getConfig().getValue("private-password");
            if(password.length() > 0) {
                event.setMotd(ChatColor.translateAlternateColorCodes('&', motdTitle) + "\n" + ChatColor.translateAlternateColorCodes('&', "&8v" + this.plugin.getVersion() + " &6〰〰 Private Workshop in Progress 〰〰"));
            } else {
                event.setMotd(ChatColor.translateAlternateColorCodes('&', motdTitle) + "\n" + ChatColor.translateAlternateColorCodes('&', "&8v" + this.plugin.getVersion() + " &f" + motdMessage));
            }
        });

        return true;
    }
}
