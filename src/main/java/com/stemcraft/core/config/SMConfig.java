package com.stemcraft.core.config;

import java.util.HashMap;
import java.util.Map;

public class SMConfig {
    /*
     * Configuration Files
     */
    private static final Map<String, SMConfigFile> configFiles = new HashMap<>();

    /**
     * Constructor
     */
    public SMConfig() {
        if (!configFiles.containsKey("config.yml")) {
            load("config.yml");
        }
    }

    /**
     * Get main configuration file
     * 
     * @return
     */
    public static SMConfigFile main() {
        if (!configFiles.containsKey("config.yml")) {
            load("config.yml");
        }

        return configFiles.get("config.yml");
    }


    /**
     * Check if specific configuration file loaded
     * 
     * @param path
     * @return
     */
    public static Boolean configLoaded(String path) {
        return configFiles.containsKey(path);
    }

    /**
     * Load a specific configuration file
     * 
     * @param path
     */
    public static void load(String path) {
        load(path, false);
    }

    /**
     * Load a specific configuration file
     * 
     * @param path
     */
    public static void load(String path, Boolean reload) {
        if (!configLoaded(path)) {
            configFiles.put(path, new SMConfigFile(path));
        } else if (reload) {
            SMConfigFile config = configFiles.getOrDefault(path, null);
            if (config != null) {
                config.reload();
            }
        }
    }

    /**
     * Get a specific configration file
     * 
     * @param path
     * @return
     */
    public static SMConfigFile getConfig(String path) {
        return configFiles.getOrDefault(path, null);
    }

    /**
     * Get a specific configration file
     * 
     * @param path
     * @return
     */
    public static SMConfigFile getOrLoadConfig(String path) {
        if (!configFiles.containsKey(path)) {
            load(path);
        }

        return configFiles.get(path);
    }

    /**
     * Reload all config files
     * 
     * @return
     */
    public static void reloadAll() {
        for (SMConfigFile file : configFiles.values()) {
            file.reload();
        }
    }
}
