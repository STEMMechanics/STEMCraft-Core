package com.stemcraft.core.event;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SMEventContext<T extends Event> {
    public Listener listener;
    public T event;
}
