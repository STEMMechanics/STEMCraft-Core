package com.stemcraft.listener.server;

import java.util.Random;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
// import com.stemcraft.config.ConfigHandler;

public class ServerListPingListener implements Listener {

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        // Random random = new Random();

        // int index = random.nextInt(ConfigHandler.config.motd.size());
        event.setMotd("Test\nTest");
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
