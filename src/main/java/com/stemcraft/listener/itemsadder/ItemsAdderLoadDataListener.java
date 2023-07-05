package com.stemcraft.listener.itemsadder;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.stemcraft.STEMCraft;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;

public class ItemsAdderLoadDataListener implements Listener {
    
    @EventHandler
    public void onItemsAdderLoadData(ItemsAdderLoadDataEvent event) {
        STEMCraft.setDependencyReady("itemsadder", true);
    }
}
