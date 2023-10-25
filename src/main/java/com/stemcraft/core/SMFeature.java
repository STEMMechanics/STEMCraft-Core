package com.stemcraft.core;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class SMFeature {
    /**
     * Is this feature enabled
     */
    private Boolean enabled = false;

    /**
     * Load this feature after the following features
     */
    @Getter
    protected List<String> loadAfterFeatures = new ArrayList<>();

    /**
     * Requires the following features to be enabled beforehand
     */
    @Getter
    protected List<String> requireFeatures = new ArrayList<>();

    /**
     * Called when the feature is to be loaded (after enable).
     * Should return true if the plugin can be enabled.
     * @param plugin
     * @return
     */
    public Boolean onLoad() {
        return true;
    }

    /**
     * Called when the feature is to be enabled. Return true if
     * the feature was successfully enabled.
     * @return
     */
    protected Boolean onEnable() {
        this.enabled = true;
        
        return this.enabled;
    }

    /**
     * Called when the feature is to be disabled.
     */
    protected void onDisable() {
        this.enabled = false;
    }

    /**
     * Get this feature name from the class name.
     * @return
     */
    public String getName() {
        String className = this.getClass().getSimpleName();
        
        if(className.equals("SMFeature")) {
            return "";
        }
        
        return className;
    }

    /**
     * Return if the feature is enabled.
     * @return
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Called to disable the feature.
     */
    public void disable() {
        if(this.isEnabled()) {
            this.onDisable();
            this.enabled = false;
        }
    }

    /**
     * Called to enable the feature.
     */
    public void enable() {
        if(!this.isEnabled()) {
            this.enabled = this.onEnable();
        }
    }
}
