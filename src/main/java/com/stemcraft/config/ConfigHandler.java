package com.stemcraft.config;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import com.stemcraft.STEMCraft;

public class ConfigHandler {
    private static String configFileName = "config.yml";

    public static String serverName = "STEMCraft";
    public static Map<String, String> motd = new HashMap<>();

    private static void loadConfig() {
        final STEMCraft plugin = STEMCraft.getInstance();
        final File configFolder = plugin.getDataFolder();
        if(!configFolder.exists()) {
            configFolder.mkdir();
        }

        final Map<String, byte[]> map = new HashMap<>();
        final File configFile = new File(configFolder, configFileName);

        if(configFile.exists()) {
            final byte[] data = Files.readAllBytes(configFile.toPath());
            map.put("config", data);
        } else {
            // ???
        }

        
    }
}
