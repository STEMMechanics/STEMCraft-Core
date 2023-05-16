package stemcraft.components;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import stemcraft.objects.SMComponent;
import java.util.List;
import java.util.Random;

public class RandomMOTD extends SMComponent {

    private String serverName;
    private List<String> motdLines;

    @Override
    public Boolean onEnable() {
        loadConfig();
        registerEvents();
        return true;
    }

    @Override
    public void onReload() {
        this.loadConfig();
    }

    /**
     * @param event
     */
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        Random random = new Random();

        int index = random.nextInt(this.motdLines.size());
        event.setMotd(this.serverName + "\n" + motdLines.get(index));
    }

    protected void loadConfig() {
        this.serverName = this.smConfig.getString(this.componentName + ".ServerName", "XMinecraft Server");
        this.motdLines = this.smConfig.getStringList(this.componentName + ".RandomMOTD");

        // Translate colors
        this.serverName = ChatColor.translateAlternateColorCodes('&', this.serverName);

        for (int i = 0; i < motdLines.size(); i++) {
            String line = motdLines.get(i);
            if (!line.startsWith("&")) {
                line = "&6" + line;
            }
            String updatedLine = ChatColor.translateAlternateColorCodes('&', line);
            motdLines.set(i, updatedLine);
        }
    }
}
