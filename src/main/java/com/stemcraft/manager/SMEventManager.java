package com.stemcraft.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

public class SMEventManager extends SMManager {
    public void registerEvent(Class<? extends Event> event, EventExecutor executor) {
        this.registerEvent(event, EventPriority.NORMAL, executor, false);
    }
    
    public void registerEvent(Class<? extends Event> event, EventExecutor executor, boolean ignoreCancelled) {
        this.registerEvent(event, EventPriority.NORMAL, executor, ignoreCancelled);
    }

    public void registerEvent(Class<? extends Event> event, EventPriority priority, EventExecutor executor) {
        this.registerEvent(event, priority, executor, false);
    }

    public void registerEvent(Class<? extends Event> event, EventPriority priority, EventExecutor executor, boolean ignoreCancelled) {
        Bukkit.getPluginManager().registerEvent(event, new Listener() {}, priority, executor, this.plugin, ignoreCancelled);
    }
}
