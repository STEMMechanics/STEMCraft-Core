package com.stemcraft.feature;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.STEMCraft;
import dev.lone.itemsadder.api.CustomStack;

public class SMItemsAdder extends SMFeature {
    private Boolean itemsAdderReady = false;

    @Override
    public Boolean onLoad(STEMCraft plugin) {
        if(!super.onLoad(plugin)) {
            return false;
        }

        if(!this.plugin.getDependManager().getDependencyLoaded("ItemsAdder")) {
            return false;
        }

        return true;
    }

    @Override
    protected Boolean onEnable() {
        this.plugin.getDependManager().onDependencyReady("ItemsAdder", () -> {
            this.itemsAdderReady = true;
        });

        return true;
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
                System.out.println(name + " is returned");
                return new ItemStack(material, quantity);
            }
        } else if(this.itemsAdderReady) {
            CustomStack customStack = CustomStack.getInstance(name);
            if(customStack != null) {
                ItemStack itemStack = customStack.getItemStack();
                itemStack.setAmount(quantity);

                System.out.println(name + " is returned");
                return itemStack;
            }
        }

        System.out.println(name + " is null");
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
}
