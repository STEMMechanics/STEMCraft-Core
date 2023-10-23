package com.stemcraft.feature;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMDependency;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;

public class SMItemsAdder extends SMFeature {
    private String dependantName = "ItemsAdder";
    private String dependantClazz = "dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent";
    private Boolean dependantReady = false;

    @Override
    public Boolean onLoad() {
        if(!super.onLoad()) {
            return false;
        }

        if(Bukkit.getPluginManager().getPlugin(dependantName) == null) {
            STEMCraft.warning(dependantName + " is not loaded. Features requiring this plugin won't be available");
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Boolean onEnable() {
        try {
            Class<? extends Event> eventClass = (Class<? extends Event>)Class.forName(dependantClazz);
            
            SMEvent.register(eventClass, ctx -> {
                dependantReady = true;
                SMDependency.setDependencyReady(dependantName);
            });
        } catch(Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public Boolean isItemsAdderReady() {
        return this.dependantReady;
    }

    public ItemStack createItemStack(String name) {
        return this.createItemStack(name, 1, true);
    }

    public ItemStack createItemStack(String name, int quantity) {
        return this.createItemStack(name, quantity, true);
    }
    
    public ItemStack createItemStack(String name, int quantity, Boolean returnEmpty) {
        if(name.startsWith("minecraft:")) {
            name = name.substring(10);
        }

        if(!name.contains(":")) {
            Material material = Material.getMaterial(name.toUpperCase());
            if(material != null) {
                return new ItemStack(material, quantity);
            }
        } else if(this.dependantReady) {
            CustomStack customStack = CustomStack.getInstance(name);
            if(customStack != null) {
                ItemStack itemStack = customStack.getItemStack();
                itemStack.setAmount(quantity);

                return itemStack;
            }
        }

        if(returnEmpty) {
            return new ItemStack(Material.AIR);
        }

        return null;
    }

    public ItemStack createItemStack(Material material) {
        return this.createItemStack(material, 1);
    }

    public ItemStack createItemStack(Material material, int quantity) {
        return new ItemStack(material, quantity);
    }

    public String formatString(Player player, String string) {
        if(this.dependantReady) {
            if(player != null) {
                return FontImageWrapper.replaceFontImages(string);
            } else {
                return FontImageWrapper.replaceFontImages(player, string);
            }
        }

        return string;
    }
}
