package com.stemcraft;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.stemcraft.manager.SMCommandManager;
import com.stemcraft.manager.SMConfigManager;
import com.stemcraft.manager.SMDatabaseManager;
import com.stemcraft.manager.SMDependManager;
import com.stemcraft.manager.SMEventManager;
import com.stemcraft.manager.SMFeatureManager;
import com.stemcraft.manager.SMLanguageManager;

public class STEMCraft extends JavaPlugin implements Listener {
    private static STEMCraft instance;
    private Boolean allowEnable = true;
    private Boolean debugEnabled = false;
    String[] requiredPlugins = {"NBTAPI", "PlaceholderAPI", "WorldEdit", "WorldGuard", "Vault"};

    private SMEventManager eventManager = new SMEventManager();
    private SMCommandManager commandManager = new SMCommandManager();
    private SMFeatureManager featureManager = new SMFeatureManager();
    private SMLanguageManager languageManager = new SMLanguageManager();
    private SMDatabaseManager databaseManager = new SMDatabaseManager();
    private SMConfigManager configManager = new SMConfigManager();
    private SMDependManager dependManager = new SMDependManager();

    private Map<UUID, BukkitRunnable> delayedTasks = new HashMap<>();


    public SMEventManager getEventManager() {
        return this.eventManager;
    }

    public SMConfigManager getConfigManager() {
        return this.configManager;
    }

    public SMDatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public SMCommandManager getCommandManager() {
        return this.commandManager;
    }

    public SMFeatureManager getFeatureManager() {
        return this.featureManager;
    }

    public SMLanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public SMDependManager getDependManager() {
        return this.dependManager;
    }

    public static STEMCraft getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        STEMCraft.instance = this;

        for (String pluginName : this.requiredPlugins) {
            if (Bukkit.getPluginManager().getPlugin(pluginName) == null) {
                getLogger().severe(pluginName + " is not installed! This plugin requires " + pluginName);
                this.allowEnable = false;
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        this.getEventManager().onLoad(this);
        this.getDependManager().onLoad(this);
        this.getConfigManager().onLoad(this);
        this.getLanguageManager().onLoad(this);
        this.getDatabaseManager().onLoad(this);
        this.getCommandManager().onLoad(this);
        this.getFeatureManager().onLoad(this);
    }

    @Override
    public void onEnable() {
        if(this.allowEnable == false) {
            getLogger().severe("STEMCraft was not enabled because a dependency was missing");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        for (String pluginName : this.requiredPlugins) {
            if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                getLogger().severe(pluginName + " is not enabled! This plugin requires " + pluginName);
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        this.getEventManager().onEnable();
        this.getDependManager().onEnable();
        this.getConfigManager().onEnable();
        this.getLanguageManager().onEnable();
        this.getDatabaseManager().onEnable();
        this.getCommandManager().onEnable();
        this.getFeatureManager().onEnable();

        this.debugEnabled = this.getConfigManager().getConfig().registerBoolean("debug", false, "Enable debug output");

        String[][] tabCompletions = new String[][]{
                {"stemcraft", "info"},
            };
            this.getCommandManager().registerStemCraftOption("version", (CommandSender sender, String[] args) -> {
                sender.sendMessage("STEMCraft " + this.getVersion());
                return true;
            }, tabCompletions);
        
        this.getConfigManager().saveAllConfigs();
    }


    @Override
    public void onDisable() {
        this.getEventManager().onDisable();
        this.getDependManager().onDisable();
        this.getConfigManager().onDisable();
        this.getLanguageManager().onDisable();
        this.getDatabaseManager().onDisable();
        this.getCommandManager().onDisable();
        this.getFeatureManager().onDisable();
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == this) {
            onDisable();
        }
    }

    public List<Class<?>> getClassList(String path, Boolean ignoreRoot) {
        List<Class<?>> classes = new ArrayList<>();

        try {
            File pluginFile = new File(STEMCraft.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (pluginFile.getPath().endsWith(".jar")) {
                JarInputStream jarInputStream = new JarInputStream(new FileInputStream(pluginFile));
                while (true) {
                    JarEntry jarEntry = jarInputStream.getNextJarEntry();
                    if (jarEntry == null) {
                        break;
                    }
                    String className = jarEntry.getName();
                    if (className.startsWith(path) && className.endsWith(".class") && (!ignoreRoot || className.indexOf('/', path.length()) != -1)) {
                        Class<?> classItem = Class.forName(className.substring(0, className.length() - 6).replaceAll("/", "."));
                        classes.add(classItem);
                    }
                }
                jarInputStream.close();
            }    
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    public <T> int delayedTask(long delay, Consumer<T> callback, T data) {
        return delayedTask(delay, null, callback, data);
    }

    public <T> int delayedTask(long delay, UUID nonce, Consumer<T> callback, T data) {
        if(nonce != null) {
            if (delayedTasks.containsKey(nonce)) {
                delayedTasks.get(nonce).cancel();
                delayedTasks.remove(nonce);
            }
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                callback.accept(data);

                if(nonce != null) {
                    delayedTasks.remove(nonce);
                } else {
                    cancelDelayedTask(this.getTaskId());
                }
            }
        };

        if(nonce != null) {
            delayedTasks.put(nonce, task);
        }

        task.runTaskLater(this, delay);
        return task.getTaskId();
    }

    public void cancelDelayedTask(UUID nonce) {
        if(nonce != null) {
            if (delayedTasks.containsKey(nonce)) {
                delayedTasks.get(nonce).cancel();
                delayedTasks.remove(nonce);
            }
        }
    }

    public void cancelDelayedTask(int taskId) {
        List<UUID> keysToRemove = new ArrayList<>();
        
        for (Map.Entry<UUID, BukkitRunnable> entry : delayedTasks.entrySet()) {
            if (entry.getValue().getTaskId() == taskId) {
                keysToRemove.add(entry.getKey());
                entry.getValue().cancel();
            }
        }
    
        for (UUID key : keysToRemove) {
            delayedTasks.remove(key);
        }
    }

    public String getVersion() {
        return this.getDescription().getVersion();
    }

    public void DebugLog(String s) {
        if(this.debugEnabled == true) {
            this.getLogger().info("DEBUG: " + s);
        }
    }
}
