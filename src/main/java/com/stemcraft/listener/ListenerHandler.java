package com.stemcraft.listener;

import org.bukkit.plugin.PluginManager;
import com.stemcraft.STEMCraft;

public final class ListenerHandler {
    public ListenerHandler(STEMCraft plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        // pluginManager.registerEvents(new BlockBreakListener(), plugin);
    }
}
