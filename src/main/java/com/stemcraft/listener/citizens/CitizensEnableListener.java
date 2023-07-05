package com.stemcraft.listener.citizens;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.stemcraft.STEMCraft;
import net.citizensnpcs.api.event.CitizensEnableEvent;

public class CitizensEnableListener implements Listener {
    
    @EventHandler
    public void onCitizensEnabled(CitizensEnableEvent event) {
        STEMCraft.setDependencyReady("citizens", true);
    }
}
