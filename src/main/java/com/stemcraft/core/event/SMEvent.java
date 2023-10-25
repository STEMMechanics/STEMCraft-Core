package com.stemcraft.core.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.stemcraft.STEMCraft;

public class SMEvent {
    public static <T extends Event> void register(Class<T> event, SMEventProcessor<T> callback) {
        SMEvent.register(event, EventPriority.NORMAL, callback, false);
    }
    
    public static <T extends Event> void register(Class<T> event, SMEventProcessor<T> callback, boolean ignoreCancelled) {
        SMEvent.register(event, EventPriority.NORMAL, callback, ignoreCancelled);
    }

    public static <T extends Event> void register(Class<T> event, EventPriority priority, SMEventProcessor<T> callback) {
        SMEvent.register(event, priority, callback, false);
    }

    public static <T extends Event> void register(Class<T> event, EventPriority priority, SMEventProcessor<T> callback, boolean ignoreCancelled) {
        Bukkit.getPluginManager().registerEvent(event, new Listener() {}, priority, (listener, rawEvent) -> {
            if (event.isInstance(rawEvent)) {
                @SuppressWarnings("unchecked")
                SMEventContext<T> context = new SMEventContext<>(listener, (T)rawEvent);
                callback.process(context);
            }
        }, STEMCraft.getPlugin(), ignoreCancelled);
    }
}
