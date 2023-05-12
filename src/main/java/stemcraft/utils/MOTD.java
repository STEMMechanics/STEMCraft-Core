package stemcraft.utils;

public import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MOTDPlugin extends JavaPlugin implements Listener {

    private List<String> motdLines;

    @Override
    public void onEnable() {
        // Create the config.yml file if it doesn't exist
        saveDefaultConfig();

        // Load MOTD lines from the configuration
        loadMotdLines();

        // Register the event listener
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Save the MOTD lines to the configuration
        saveMotdLines();
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        // Get a random MOTD line from the motdLines list
        String randomLine1 = getRandomMotdLine();
        String randomLine2 = getRandomMotdLine();

        // Set the random MOTD lines
        event.setMotd(randomLine1 + "\n" + randomLine2);
    }

    private void loadMotdLines() {
        // Get the configuration file
        FileConfiguration config = getConfig();

        // Load the MOTD line from the configuration
        String motdLine = config.getString("motd-line", "&aWelcome to the server!\\n&7Enjoy your stay!");

        // Translate color codes and split into lines if separator exists
        motdLines = new ArrayList<>();
        String[] lines = motdLine.split("\\\\n");
        for (String line : lines) {
            motdLines.add(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    private void saveMotdLines() {
        // Get the configuration file
        FileConfiguration config = getConfig();

        // Join the MOTD lines into a single string
        String motdLine = String.join("\\n", motdLines);

        // Set the MOTD line in the configuration
        config.set("motd-line", motdLine);

        // Save the configuration to the file
        saveConfig();
    }

    private String getRandomMotdLine() {
        // Get a random index from the motdLines list
        Random random = new Random();
        int index = random.nextInt(motdLines.size());

        // Return the MOTD line at the random index
        return motdLines.get(index);
    }
}
