package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMItemLore;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMReplacer;

public class SMToolStats extends SMFeature {
    private final NamespacedKey blocksMined = new NamespacedKey(STEMCraft.getPlugin(), "blocks-mined");
    private final NamespacedKey cropsHarvested = new NamespacedKey(STEMCraft.getPlugin(), "crops-harvested");
    private final NamespacedKey fishingRodCaught = new NamespacedKey(STEMCraft.getPlugin(), "fish-caught");
    private final NamespacedKey arrowsShot = new NamespacedKey(STEMCraft.getPlugin(), "arrows-shot");
    private final NamespacedKey sheepSheared = new NamespacedKey(STEMCraft.getPlugin(), "sheep-sheared");

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMItemLore.register("toolstats", (item) -> {
            List<String> lore = new ArrayList<>();
            
            // blocks-mined
            Integer blocksMined = getBlocksMined(item);
            if(blocksMined != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-BLOCKS-MINED"), "blocks", blocksMined.toString()));
            }

            // crops-mined
            Integer cropsMined = getCropsHarvested(item);
            if(cropsMined != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-CROPS-MINED"), "crops", cropsMined.toString()));
            }

            // fish-caught
            Integer fishCaught = getFishCaught(item);
            if(fishCaught != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-FISH-CAUGHT"), "fish", fishCaught.toString()));
            }

            // arrows-shot
            Integer arrowsShot = getArrowsShot(item);
            if(arrowsShot != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-ARROWS-SHOT"), "arrows", arrowsShot.toString()));

            }

            // sheep-sheared
            Integer sheepSheared = getSheepSheared(item);
            if(sheepSheared != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-SHEEP-SHEARED"), "sheep", sheepSheared.toString()));

            }

            return lore;
        });

        SMEvent.register(BlockBreakEvent.class, EventPriority.MONITOR, (ctx) -> {
            if(ctx.event.isCancelled()) {
                return;
            }

            Player player = ctx.event.getPlayer();
            if(player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            PlayerInventory inventory = player.getInventory();
            ItemStack heldItem = inventory.getItemInMainHand();
            Block block = ctx.event.getBlock();
            
            if(isMiningTool(heldItem.getType())) {
                if(isHarvestingTool(heldItem.getType()) && block.getBlockData() instanceof Ageable) {
                    Ageable ageableBlock = (Ageable)block.getBlockData();

                    if(ageableBlock.getAge() >= ageableBlock.getMaximumAge()) {
                        updateCropsHarvested(heldItem);
                    }
                } else {
                    updateBlocksMined(heldItem);
                }
            }
        });
        
        SMEvent.register(PlayerFishEvent.class, EventPriority.MONITOR, (ctx) -> {
            if(ctx.event.isCancelled()) {
                return;
            }

            if (ctx.event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
                return;
            }

            Player player = ctx.event.getPlayer();
            if(player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            PlayerInventory inventory = player.getInventory();
            boolean isMainHand = isFishingItem(inventory.getItemInMainHand().getType());
            boolean isOffHand = isFishingItem(inventory.getItemInOffHand().getType());
            ItemStack fishingRod = null;
            if (isOffHand) {
                fishingRod = inventory.getItemInOffHand();
            }
            if (isMainHand) {
                fishingRod = inventory.getItemInMainHand();
            }
            if (fishingRod == null) {
                return;
            }

            updateFishCaught(fishingRod);
        });

        SMEvent.register(EntityShootBowEvent.class, EventPriority.MONITOR, (ctx) -> {
            Entity shooter = ctx.event.getEntity();
            if (!(shooter instanceof Player)) {
                return;
            }
            
            if(ctx.event.isCancelled()) {
                return;
            }

            Player player = (Player)shooter;
            if(player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            PlayerInventory inventory = player.getInventory();
            boolean isMainHand = isBowItem(inventory.getItemInMainHand().getType());
            boolean isOffHand = isBowItem(inventory.getItemInOffHand().getType());
            ItemStack heldBow = null;
            if (isOffHand) {
                heldBow = inventory.getItemInOffHand();
            }
            if (isMainHand) {
                heldBow = inventory.getItemInMainHand();
            }
            if (heldBow == null) {
                return;
            }

            updateArrowsShot(heldBow);
        });

        SMEvent.register(PlayerInteractEntityEvent.class, EventPriority.MONITOR, (ctx) -> {
            if(ctx.event.isCancelled()) {
                return;
            }

            Player player = ctx.event.getPlayer();
            if(player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            Entity entity = ctx.event.getRightClicked();
            if (!(entity instanceof Sheep)) {
                return;
            }

            PlayerInventory inventory = player.getInventory();
            boolean isMainHand = isShearingItem(inventory.getItemInMainHand().getType());
            boolean isOffHand = isShearingItem(inventory.getItemInOffHand().getType());
            ItemStack shears = null;
            if (isOffHand) {
                shears = inventory.getItemInOffHand();
            }
            if (isMainHand) {
                shears = inventory.getItemInMainHand();
            }
            if (shears == null) {
                return;
            }

            Sheep sheep = (Sheep) entity;
            if (sheep.isSheared()) {
                return;
            }

            updateSheepSheared(shears);
        });

        return true;
    }

    /**
     * Update the number of blocks mined by this tool. Returns the mined count or null.
     * @param tool
     */
    private Integer updateBlockCountData(ItemStack tool, NamespacedKey key, Integer increase) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return null;
        }

        Integer value = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.INTEGER)) {
            value = container.get(key, PersistentDataType.INTEGER);
            if(value == null) {
                value = 0;
            }
        }

        if(increase != 0) {
            value += increase;
            container.set(key, PersistentDataType.INTEGER, value);
            tool.setItemMeta(meta);
            SMItemLore.updateLore(tool);
        }

        return value;
    }

    /**
     * Get the number of blocks mined with this tool
     * @param tool
     * @return
     */
    public Integer getBlocksMined(ItemStack tool) {
        return updateBlocksMined(tool, 0);
    }

    /**
     * Update the number of blocks mined by this tool by 1
     * @param tool
     */
    public void updateBlocksMined(ItemStack tool) {
        updateBlocksMined(tool, 1);
    }

    /**
     * Update the number of blocks mined by this tool. Returns the mined count or null.
     * @param tool
     */
    private Integer updateBlocksMined(ItemStack tool, Integer increase) {
        if(!isMiningTool(tool.getType())) {
            return null;
        }
        
        return updateBlockCountData(tool, blocksMined, increase);
    }

    /**
     * Get the number of crops mined with this tool
     * @param tool
     * @return
     */
    public Integer getCropsHarvested(ItemStack tool) {
        return updateCropsHarvested(tool, 0);
    }

    /**
     * Update the number of crops mined by this tool by 1
     * @param tool
     */
    public void updateCropsHarvested(ItemStack tool) {
        updateCropsHarvested(tool, 1);
    }

    /**
     * Update the number of blocks mined by this tool. Returns the mined count or null.
     * @param tool
     */
    private Integer updateCropsHarvested(ItemStack tool, Integer increase) {
        if(!isHarvestingTool(tool.getType())) {
            return null;
        }
        
        return updateBlockCountData(tool, cropsHarvested, increase);
    }

    /**
     * Get the number of fish caught with this tool
     * @param tool
     * @return
     */
    public Integer getFishCaught(ItemStack tool) {
        return updateFishCaught(tool, 0);
    }

    /**
     * Update the number of fish caught by this tool by 1
     * @param tool
     */
    public void updateFishCaught(ItemStack tool) {
        updateCropsHarvested(tool, 1);
    }

    /**
     * Update the number of fish caught by this tool. Returns the count or null.
     * @param tool
     */
    private Integer updateFishCaught(ItemStack tool, Integer increase) {
        if(!isFishingItem(tool.getType())) {
            return null;
        }
        
        return updateBlockCountData(tool, fishingRodCaught, increase);
    }

    /**
     * Get the number of arrows shot with this tool
     * @param tool
     * @return
     */
    public Integer getArrowsShot(ItemStack tool) {
        return updateArrowsShot(tool, 0);
    }

    /**
     * Update the number of arrows shot by this tool by 1
     * @param tool
     */
    public void updateArrowsShot(ItemStack tool) {
        updateArrowsShot(tool, 1);
    }

    /**
     * Update the number of arrows shot by this tool. Returns the count or null.
     * @param tool
     */
    private Integer updateArrowsShot(ItemStack tool, Integer increase) {
        if(!isBowItem(tool.getType())) {
            return null;
        }
        
        return updateBlockCountData(tool, arrowsShot, increase);
    }

    /**
     * Get the number of sheep sheared with this tool
     * @param tool
     * @return
     */
    public Integer getSheepSheared(ItemStack tool) {
        return updateSheepSheared(tool, 0);
    }

    /**
     * Update the number of sheep sheared by this tool by 1
     * @param tool
     */
    public void updateSheepSheared(ItemStack tool) {
        updateSheepSheared(tool, 1);
    }

    /**
     * Update the number of sheep sheared by this tool. Returns the count or null.
     * @param tool
     */
    private Integer updateSheepSheared(ItemStack tool, Integer increase) {
        if(!isShearingItem(tool.getType())) {
            return null;
        }
        
        return updateBlockCountData(tool, sheepSheared, increase);
    }

    /**
     * Is material a mining tool?
     * @param type
     * @return
     */
    public Boolean isMiningTool(Material type) {
        String lowerCase = type.toString().toLowerCase(Locale.ROOT);

        return (type == Material.SHEARS || lowerCase.contains("_pickaxe") || lowerCase.contains("_axe") || lowerCase.contains("_hoe") || lowerCase.contains("_shovel"));
    }

    /**
     * Is material a harvesting tool?
     * @param type
     * @return
     */
    public Boolean isHarvestingTool(Material type) {
        return type.toString().toLowerCase(Locale.ROOT).contains("_hoe");
    }

    /**
     * Is material an armor item
     * @param type
     * @return
     */
    public Boolean isArmor(Material type) {
        String lowerCase = type.toString().toLowerCase(Locale.ROOT);

        return (lowerCase.contains("_helmet") || lowerCase.contains("_chestplate") || lowerCase.contains("_leggings") || lowerCase.contains("_boots"));
    }

    /**
     * Is material a melee weapon
     * @param type
     * @return
     */
    public Boolean isMeleeWeapon(Material type) {
        String lowerCase = type.toString().toLowerCase(Locale.ROOT);

        return (type == Material.TRIDENT || lowerCase.contains("_sword") || lowerCase.contains("_axe"));
    }

     /**
     * Is material a fishing item
     * @param type
     * @return
     */
    public Boolean isFishingItem(Material type) {
        return type == Material.FISHING_ROD;
    }

     /**
     * Is material a bow item
     * @param type
     * @return
     */
    public Boolean isBowItem(Material type) {
        return type == Material.BOW || type == Material.CROSSBOW;
    }

     /**
     * Is material a shearing item
     * @param type
     * @return
     */
    public Boolean isShearingItem(Material type) {
        return type == Material.SHEARS;
    }

    public Boolean isTrackedItem(Material type) {
        return (isMiningTool(type) || isHarvestingTool(type) || isArmor(type) || isMeleeWeapon(type) || isFishingItem(type) || isBowItem(type));
    }
}