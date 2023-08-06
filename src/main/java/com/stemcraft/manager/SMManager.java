package com.stemcraft.manager;

import com.stemcraft.STEMCraft;

public class SMManager {
    protected STEMCraft plugin = null;

    public void onLoad(STEMCraft plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        /* empty */
    }

    public void onDisable() {
        /* empty */
    }
}
