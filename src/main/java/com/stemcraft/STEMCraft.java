package com.stemcraft;

import org.bukkit.plugin.java.JavaPlugin;
import com.stemcraft.listener.ListenerHandler;

public class STEMCraft extends JavaPlugin {
    private static STEMCraft instance;

    public static STEMCraft getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        try {
            new ListenerHandler(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
