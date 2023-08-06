package com.stemcraft.feature;

import com.stemcraft.STEMCraft;

public class SMFeature {
    protected STEMCraft plugin = null;
    private Boolean enabled = false;

    public Boolean onLoad(STEMCraft plugin) {
        this.plugin = plugin;
        return true;
    }

    protected Boolean onEnable() {
        this.enabled = true;
        
        return this.enabled;
    }

    protected void onDisable() {
        this.enabled = false;
    }

    public String getName() {
        String className = this.getClass().getSimpleName();
        
        if(className.equals("SMFeature")) {
            return "";
        }
        
        return className;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void disable() {
        if(this.isEnabled()) {
            this.onDisable();
            this.enabled = false;
        }
    }

    public void enable() {
        if(!this.isEnabled()) {
            this.enabled = this.onEnable();
        }
    }

    public void enable(Boolean enable) {
        if(enable) {
            this.enable();
        } else {
            this.disable();
        }
    }
}
