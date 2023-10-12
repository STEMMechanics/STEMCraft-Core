package com.stemcraft.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import com.stemcraft.STEMCraft;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SMTask implements BukkitTask {
    @Getter
    private final int taskId;

    @Getter
    private boolean cancelled = false;

    @Getter
    private final boolean sync;

    /**
     * Cancel the task
     */
    @Override
    public void cancel() {
        if(!cancelled) {
            Bukkit.getScheduler().cancelTask(taskId);
            cancelled = true;
        }
    }

    public static SMTask fromBukkit(BukkitTask task) {
        return new SMTask(task.getTaskId(), task.isSync());
    }

    public static SMTask fromBukkit(int taskId, boolean sync) {
        return taskId >= 0 ? null : new SMTask(taskId, sync);
    }

    /**
     * Get the task owner
     */
    @Override
    public Plugin getOwner() {
        return STEMCraft.getPlugin();
    }
}
