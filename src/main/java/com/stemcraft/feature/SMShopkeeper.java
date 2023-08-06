package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import com.stemcraft.STEMCraft;
import com.stemcraft.trait.SMTraitShopkeeper;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;

public class SMShopkeeper extends SMFeature {
    SMItemsAdder iaFeature = null;

    @Override
    public Boolean onLoad(STEMCraft plugin) {
        if(!super.onLoad(plugin)) {
            return false;
        }

        if(!this.plugin.getDependManager().getDependencyLoaded("Citizens")) {
            return false;
        }

        return true;
    }

    @Override
    protected Boolean onEnable() {
        this.iaFeature = (SMItemsAdder)this.plugin.getFeatureManager().getFeature("SMItemsAdder");

        CitizensAPI.getTraitFactory().registerTrait(
            TraitInfo.create(SMTraitShopkeeper.class).withName("shopkeeper")
        );

        this.plugin.getEventManager().registerEvent(NPCRightClickEvent.class, (listener, rawEvent) -> {
            NPCRightClickEvent event = (NPCRightClickEvent)rawEvent;
            NPC npc = event.getNPC();

            if(npc.hasTrait(SMTraitShopkeeper.class) && event.getClicker() instanceof Player) {
                Player player = event.getClicker();

                Merchant merchant = player.getServer().createMerchant("Shopkeeper");
                List<MerchantRecipe> trades = new ArrayList<>();
            
                ItemStack coal = iaFeature.createItemStack("minecraft:coal", 9);
                ItemStack boat = iaFeature.createItemStack(Material.ACACIA_BOAT, 1);
                ItemStack emerald = iaFeature.createItemStack("stemcraft:grave");
                
                MerchantRecipe trade = new MerchantRecipe(emerald, 1);
                trade.addIngredient(coal);
                trade.addIngredient(boat);
                
                trades.add(trade);
                merchant.setRecipes(trades);

                player.openMerchant(merchant, true);
            }
        });

        return true;
    }
}
