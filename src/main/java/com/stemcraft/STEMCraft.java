package com.stemcraft;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.bukkit.plugin.java.JavaPlugin;
import com.stemcraft.command.CommandHandler;
import com.stemcraft.database.DatabaseHandler;
import com.stemcraft.listener.ListenerHandler;

public class STEMCraft extends JavaPlugin {
    private static STEMCraft instance;
    private static DatabaseHandler databaseHandler = null;

    public static STEMCraft getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        try {
            File dataFolder = this.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            databaseHandler = new DatabaseHandler(this);
            databaseHandler.connect();

            new ListenerHandler(this);
            new CommandHandler(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        databaseHandler.disconnect();
    }

    public static DatabaseHandler database() {
        return databaseHandler;
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
