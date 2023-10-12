package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;
import de.tr7zw.nbtapi.NBT;

public class SMNoIncreaseExpCost extends SMFeature {

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(InventoryClickEvent.class, ctx -> {
            if (ctx.event.getWhoClicked().getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            if ((ctx.event.getInventory() instanceof AnvilInventory)) {
                // Do not increase experiance costs on anvils
                ItemStack item = ctx.event.getCurrentItem();
                if (item != null && item.getType() != Material.AIR) {
                    NBT.modify(item, nbt -> {
                        nbt.removeKey("RepairCost");
                    });
                }
            }
        });

        return true;
    }
}
