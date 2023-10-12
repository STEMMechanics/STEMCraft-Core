package com.stemcraft.feature;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.core.SMDependency;
import com.stemcraft.core.SMFeature;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;

public class SMItemsAdder extends SMFeature {
    private Boolean itemsAdderReady = false;

    @Override
    public Boolean onLoad() {
        if(!super.onLoad()) {
            return false;
        }

        if(!SMDependency.dependencyLoaded("ItemsAdder")) {
            return false;
        }

        return true;
    }

    @Override
    protected Boolean onEnable() {
        SMDependency.onDependencyReady("ItemsAdder", () -> {
            this.itemsAdderReady = true;
        });

        return true;
    }

    public Boolean isItemsAdderReady() {
        return this.itemsAdderReady;
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
        } else if(this.itemsAdderReady) {
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
        if(this.itemsAdderReady) {
            if(player != null) {
                return FontImageWrapper.replaceFontImages(string);
            } else {
                return FontImageWrapper.replaceFontImages(player, string);
            }
        }

        return string;
    }
}
