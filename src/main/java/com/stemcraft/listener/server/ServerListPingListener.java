package com.stemcraft.listener.server;

import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
// import com.stemcraft.config.ConfigHandler;
import com.stemcraft.utility.Meta;

public class ServerListPingListener implements Listener {

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        // Random random = new Random();

        // int index = random.nextInt(ConfigHandler.config.motd.size());
        String motd = "&6Now on 1.20.1";

        if(Meta.getBoolean("maintenance", false)) {
            motd = "&6Server under maintenance";
        }

        event.setMotd(ChatColor.translateAlternateColorCodes('&', motd));
    }

    // protected void loadConfig() {
    //     this.serverName = this.smConfig.getString(this.componentName + ".ServerName", "XMinecraft Server");
    //     this.motdLines = this.smConfig.getStringList(this.componentName + ".RandomMOTD");

    //     // Translate colors
    //     this.serverName = ChatColor.translateAlternateColorCodes('&', this.serverName);

    //     for (int i = 0; i < motdLines.size(); i++) {
    //         String line = motdLines.get(i);
    //         if (!line.startsWith("&")) {
    //             line = "&6" + line;
    //         }
    //         String updatedLine = ChatColor.translateAlternateColorCodes('&', line);
    //         motdLines.set(i, updatedLine);
    //     }
    // }
}
