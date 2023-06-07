package stemcraft;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import stemcraft.components.ConsolePlayerDeathLocation;
import stemcraft.components.CoordsHUD;
import stemcraft.components.DropMobHeads;
import stemcraft.components.DropPlayerHeads;
import stemcraft.components.NightVision;
import stemcraft.components.ReduceNetheriteChance;
import stemcraft.components.NoDespawnOnDeath;
import stemcraft.components.PreventEndermanPickups;
import stemcraft.components.RandomMOTD;
import stemcraft.objects.SMCommand;
import stemcraft.objects.SMComponent;
import stemcraft.objects.SMPlayer;

public class STEMCraft extends JavaPlugin implements Listener {
    private FileConfiguration config;
    private Map<String, SMComponent> componentMap;
    private Map<UUID, SMPlayer> playerMap;
    private Connection database;

    public STEMCraft() {
        super();
        this.componentMap = new HashMap<String, SMComponent>() {
            {
                put("ConsolePlayerDeathLocation", new ConsolePlayerDeathLocation());
                put("CoordsHUD", new CoordsHUD());
                put("DropMobHeads", new DropMobHeads());
                put("DropPlayerHeads", new DropPlayerHeads());
                put("NightVision", new NightVision());
                put("NoAncientDebris", new ReduceNetheriteChance());
                put("NoDespawnOnDeath", new NoDespawnOnDeath());
                put("PreventEndermanPickups", new PreventEndermanPickups());
                put("RandomMOTD", new RandomMOTD());
                put("ReduceNetheriteChance", new ReduceNetheriteChance());
            }
        };

        this.playerMap = new HashMap<>();
    }

    public Connection getDatabase() {
        return database;
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            database = DriverManager.getConnection("jdbc:sqlite:" + dataFolder + "/dataa.db");
            createTable();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        // Save player data every 10 minutes (20 ticks * 60 seconds * 10 minutes)
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllPlayerData();
            }
        }.runTaskTimer(this, 0L, 20L * 60L * 10L);

        this.config = getConfig();

        saveDefaultConfig();

        // Load components
        componentMap.forEach((key, value) -> {
            Boolean enabled = Boolean.FALSE;

            if (getConfig().getBoolean(key + ".enabled", Boolean.FALSE) == Boolean.TRUE) {
                enabled = value.initialize(this).enable();
            }

            getLogger().info(key + (enabled == Boolean.TRUE ? " enabled" : " disabled"));
        });

    }

    @Override
    public void onDisable() {
        try {
            if (database != null && !database.isClosed()) {
                database.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == this) {
            saveAllPlayerData();
        }
    }

    /**
     * Returns a plugin component or null if it does not exist.
     * 
     * @param componentName The component name.
     * @return The component object.
     */
    public SMComponent getComponent(String componentName) {
        return componentMap.get(componentName);
    }

    protected Map<String, BukkitCommand> commandMap = new HashMap();

    public void registerCommand(String command, String permission, String description, String usage,
            SMComponent component) {

        ArrayList<String> aliases = new ArrayList<>();
        aliases.add(command);

        this.registerCommand(aliases, permission, description, usage, component);
    }

    /**
     * Register a new Command
     * 
     * @param aliases
     * @param permission
     * @param description
     * @param usage
     */
    public void registerCommand(ArrayList<String> aliases, String permission, String description, String usage,
            SMComponent component) {
        unregisterCommand(aliases.get(0));
        SMCommand smCommand = new SMCommand(permission, aliases.get(0), description, usage, aliases, component);
        commandMap.put(aliases.get(0), smCommand);
    }

    public void unregisterCommand(String command) {
        BukkitCommand bukkitCommand = commandMap.get(command);
        if (bukkitCommand != null) {
            bukkitCommand.unregister(getBukkitCommandMap());
            commandMap.remove(command);
        }
    }

    public CommandMap getBukkitCommandMap() {
        CommandMap bukkitCommandMap = null;

        try {
            Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            bukkitCommandMap = (CommandMap) f.get(Bukkit.getPluginManager());
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }

        return bukkitCommandMap;
    }

    /**
     * 
     * @param player
     * @return
     */
    public SMPlayer getPlayer(Player player) {
        SMPlayer smPlayer;
        UUID playerId = player.getUniqueId();

        if (playerMap.containsKey(playerId)) {
            return playerMap.get(playerId);
        } else {
            smPlayer = new SMPlayer(this, player);

            smPlayer.loadData();
            playerMap.put(playerId, smPlayer);

        }

        return smPlayer;
    }

    private void saveAllPlayerData() {
        for (SMPlayer smPlayer : playerMap.values()) {
            smPlayer.saveData();
        }
    }


}
