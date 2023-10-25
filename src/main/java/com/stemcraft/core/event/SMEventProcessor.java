package com.stemcraft.core.event;

import org.bukkit.event.Event;

@FunctionalInterface
public interface SMEventProcessor<T extends Event> {
    void process(SMEventContext<T> context);
}
