package com.stemcraft;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.stemcraft.api.SMApi;
import com.stemcraft.command.SMCommand;
import com.stemcraft.component.SMComponent;
import com.stemcraft.config.SMConfig;
import com.stemcraft.database.SMDatabase;
import com.stemcraft.listener.ListenerHandler;

public class STEMCraft extends JavaPlugin implements Listener {
    private static STEMCraft instance;

    public static STEMCraft getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("ProtocolLib is not installed! This plugin requires ProtocolLib.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            File dataFolder = this.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            SMConfig.loadValues();
            SMDatabase.connect();

            new ListenerHandler(this);
            
            SMComponent.loadComponents();
            SMCommand.loadCommands();
            SMApi.loadServer();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        SMDatabase.disconnect();
        SMApi.stopServer();
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == this) {
            onDisable();
        }
    }

    public static List<Class<?>> getClassList(String path, Boolean ignoreRoot) {
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
}
