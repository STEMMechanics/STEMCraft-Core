package com.stemcraft.utility;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.stemcraft.STEMCraft;
import java.util.function.Consumer;

public class Timer<T> {
    public static <T> void start(long delay, Consumer<T> callback, T data) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                callback.accept(data); // Invoke the callback with the passed data
            }
        }.runTaskLater(STEMCraft.getInstance(), delay);
    }
}
