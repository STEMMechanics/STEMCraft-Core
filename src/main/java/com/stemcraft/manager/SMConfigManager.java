package com.stemcraft.manager;

import java.util.HashMap;
import org.bukkit.command.CommandSender;
import com.stemcraft.SMConfig;

public class SMConfigManager extends SMManager {
    private String defaultConfig = "config";
    private HashMap<String, SMConfig> configMap = new HashMap<>();

    @Override
    public void onEnable() {
        this.plugin.getLanguageManager().registerPhrase("CONFIG_RELOADED", "Configuration reloaded");

        SMConfig defaultConfig = this.getConfig(this.defaultConfig);
        defaultConfig.setHeader("STEMMechanics Config File");

        this.plugin.getCommandManager().registerStemCraftOption("reload", (CommandSender sender, String[] args) -> {
            this.configMap.forEach((key, config) -> {
                config.loadConfig();
            });

            this.plugin.getLanguageManager().sendPhrase(sender, "CONFIG_RELOADED");
            return true;
        });
    }

    public SMConfig getConfig() {
        return this.getConfig(this.defaultConfig);
    }

    public SMConfig getConfig(String name) {
        SMConfig config = null;

        if(!configMap.containsKey(name)) {
            config = new SMConfig(this.plugin, name);
            configMap.put(name, config);
            config.loadConfig();
        } else {
            config = configMap.get(name);
        }

        return config;
    }

    public void saveAllConfigs() {
        for (SMConfig smConfig : configMap.values()) {
            smConfig.saveConfig();
        }        
    }
}
