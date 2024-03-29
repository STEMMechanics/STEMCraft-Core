package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.core.persistentDataTypes.SMPersistentUUIDDataType;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMItemLore;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMReplacer;
import com.stemcraft.core.command.SMCommand;

public class SMToolStats extends SMFeature {
    private final NamespacedKey blocksMined = new NamespacedKey(STEMCraft.getPlugin(), "blocks-mined");
    private final NamespacedKey cropsHarvested = new NamespacedKey(STEMCraft.getPlugin(), "crops-harvested");
    private final NamespacedKey fishingRodCaught = new NamespacedKey(STEMCraft.getPlugin(), "fish-caught");
    private final NamespacedKey arrowsShot = new NamespacedKey(STEMCraft.getPlugin(), "arrows-shot");
    private final NamespacedKey sheepSheared = new NamespacedKey(STEMCraft.getPlugin(), "sheep-sheared");
    private final NamespacedKey itemCreated = new NamespacedKey(STEMCraft.getPlugin(), "time-created");
    private final NamespacedKey itemOwner = new NamespacedKey(STEMCraft.getPlugin(), "owner");
    /**
     * Stores how an item was created. 0 = crafted. 1 = dropped. 2 = looted. 3 = traded. 4 = founded (for elytras). 5 =
     * fished. 6 = spawned in (creative).
     */
    public final NamespacedKey originType = new NamespacedKey(STEMCraft.getPlugin(), "origin");
    public final NamespacedKey playerKills = new NamespacedKey(STEMCraft.getPlugin(), "player-kills");
    public final NamespacedKey mobKills = new NamespacedKey(STEMCraft.getPlugin(), "mob-kills");

    private final List<EntityDamageEvent.DamageCause> ignoredDamagaCauses =
        Arrays.asList(EntityDamageEvent.DamageCause.SUICIDE, EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.CUSTOM);


    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMItemLore.register("toolstats", (item) -> {
            List<String> lore = new ArrayList<>();

            // created-at
            Long createdAt = getItemCreatedAt(item);
            if (createdAt != null) {
                Date formattedDate = new Date(createdAt);
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-CREATED-AT"), "date",
                    SMCommon.formatDate(formattedDate)));
            }

            // created-by
            Player createdBy = getItemCreatedBy(item);
            if (createdBy != null) {
                lore.add(
                    SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-CREATED-BY"), "name", createdBy.getName()));
            }

            // player-kills
            Integer playerKills = itemGetPlayerKills(item);
            if (playerKills != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-PLAYER-KILLS"), "count",
                    SMCommon.formatInt(playerKills)));
            }

            // mob-kills
            Integer mobKills = itemGetMobKills(item);
            if (mobKills != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-MOB-KILLS"), "count",
                    SMCommon.formatInt(mobKills)));
            }

            // blocks-mined
            Integer blocksMined = getBlocksMined(item);
            if (blocksMined != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-BLOCKS-MINED"), "blocks",
                    SMCommon.formatInt(blocksMined)));
            }

            // crops-mined
            Integer cropsMined = getCropsHarvested(item);
            if (cropsMined != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-CROPS-HARVESTED"), "crops",
                    SMCommon.formatInt(cropsMined)));
            }

            // fish-caught
            Integer fishCaught = getFishCaught(item);
            if (fishCaught != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-FISH-CAUGHT"), "fish",
                    SMCommon.formatInt(fishCaught)));
            }

            // arrows-shot
            Integer arrowsShot = getArrowsShot(item);
            if (arrowsShot != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-ARROWS-SHOT"), "arrows",
                    SMCommon.formatInt(arrowsShot)));

            }

            // sheep-sheared
            Integer sheepSheared = getSheepSheared(item);
            if (sheepSheared != null) {
                lore.add(SMReplacer.replaceVariables(SMLocale.get("TOOLSTATS-SHEEP-SHEARED"), "sheep",
                    SMCommon.formatInt(sheepSheared)));

            }

            return lore;
        });

        new SMCommand("toolreset")
            .permission("stemcraft.command.toolreset")
            .tabComplete("{player}")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);

                if (ctx.fromConsole()) {
                    ctx.checkArgsLocale(1, "CMD_PLAYER_REQ_FROM_CONSOLE");
                }

                if (ctx.args.size() > 1 || !SMCommon.isSamePlayer(ctx.player, targetPlayer)) {
                    ctx.checkPermission("stemcraft.command.toolreset.other");
                }

                ItemStack tool = targetPlayer.getInventory().getItemInMainHand();
                if (tool == null || tool.getType() == Material.AIR) {
                    ctx.returnErrorLocale("TOOLSTATS-RESET-FAIL");
                } else {
                    clearStats(tool);
                    ctx.returnInfoLocale("TOOLSTATS-RESET");
                }
            })
            .register();

        SMEvent.register(BlockBreakEvent.class, EventPriority.MONITOR, (ctx) -> {
            if (ctx.event.isCancelled()) {
                return;
            }

            Player player = ctx.event.getPlayer();
            if (player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            PlayerInventory inventory = player.getInventory();
            ItemStack heldItem = inventory.getItemInMainHand();
            Block block = ctx.event.getBlock();

            if (isMiningTool(heldItem.getType())) {
                if (isHarvestingTool(heldItem.getType()) && block.getBlockData() instanceof Ageable) {
                    Ageable ageableBlock = (Ageable) block.getBlockData();

                    if (ageableBlock.getAge() >= ageableBlock.getMaximumAge()) {
                        updateCropsHarvested(heldItem);
                    }
                } else {
                    updateBlocksMined(heldItem);
                }
            }
        });

        SMEvent.register(PlayerFishEvent.class, EventPriority.MONITOR, (ctx) -> {
            if (ctx.event.isCancelled()) {
                return;
            }

            if (ctx.event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
                return;
            }

            Player player = ctx.event.getPlayer();
            if (player.getGameMode() != GameMode.SURVIVAL) {
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

            if (ctx.event.isCancelled()) {
                return;
            }

            Player player = (Player) shooter;
            if (player.getGameMode() != GameMode.SURVIVAL) {
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
            if (ctx.event.isCancelled()) {
                return;
            }

            Player player = ctx.event.getPlayer();
            if (player.getGameMode() != GameMode.SURVIVAL) {
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

        SMEvent.register(CraftItemEvent.class, EventPriority.HIGHEST, ctx -> {
            if (ctx.event.isCancelled()) {
                return;
            }

            Player player = (Player) ctx.event.getWhoClicked();
            if (player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            ItemStack itemStack = ctx.event.getCurrentItem();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return;
            }

            if (!isTrackedItem(itemStack.getType())) {
                return;
            }

            // Apply to all items of the same type that have been added
            final Material material = itemStack.getType();
            HashMap<Integer, ItemStack> origItems = (HashMap<Integer, ItemStack>) player.getInventory().all(material);

            STEMCraft.runLater(1, () -> {
                HashMap<Integer, ItemStack> newItems =
                    (HashMap<Integer, ItemStack>) player.getInventory().all(material);

                for (Map.Entry<Integer, ItemStack> entry : newItems.entrySet()) {
                    Integer key = entry.getKey();
                    ItemStack newItem = entry.getValue();

                    // Check if this key does not exist in the origItems map
                    if (!origItems.containsKey(key)) {
                        setItemCreatedAt(newItem);
                        setItemCreatedBy(newItem, player);
                    }
                }
            });

            setItemCreatedAt(itemStack);
            setItemCreatedBy(itemStack, player);
        });

        SMEvent.register(EntityDamageByEntityEvent.class, EventPriority.MONITOR, ctx -> {
            if (ctx.event.isCancelled()) {
                return;
            }

            if (!(ctx.event.getEntity() instanceof LivingEntity)) {
                return;
            }

            LivingEntity mobBeingAttacked = (LivingEntity) ctx.event.getEntity();
            EntityDamageEvent.DamageCause cause = ctx.event.getCause();
            if (ignoredDamagaCauses.contains(cause)) {
                return;
            }

            // mob is going to die
            if (mobBeingAttacked.getHealth() - ctx.event.getFinalDamage() <= 0) {
                // a player is killing something
                if (ctx.event.getDamager() instanceof Player) {
                    Player attackingPlayer = (Player) ctx.event.getDamager();
                    if (attackingPlayer.getGameMode() != GameMode.SURVIVAL) {
                        return;
                    }

                    PlayerInventory attackingPlayerInventory = attackingPlayer.getInventory();
                    ItemStack heldItem = attackingPlayerInventory.getItemInMainHand();
                    // only check certain items
                    if (!isMeleeWeapon(heldItem.getType())) {
                        return;
                    }
                    // a player is killing another player
                    if (mobBeingAttacked instanceof Player) {
                        itemAddPlayerKill(heldItem);
                        return;
                    }
                    // player is killing regular mob
                    itemAddMobKill(heldItem);
                }

                // trident is being thrown at something
                if (ctx.event.getDamager() instanceof Trident) {
                    Trident trident = (Trident) ctx.event.getDamager();
                    ItemStack newTrident = trident.getItem().clone();
                    // trident is killing player
                    if (mobBeingAttacked instanceof Player) {
                        itemAddPlayerKill(newTrident);
                    } else {
                        // trident is killing a mob
                        itemAddMobKill(newTrident);
                    }
                    if (newTrident != null) {
                        trident.setItem(newTrident);
                    }
                }
                // arrow is being shot
                if (ctx.event.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) ctx.event.getDamager();
                    // if the shooter is a player
                    if (arrow.getShooter() instanceof Player) {
                        Player shootingPlayer = (Player) arrow.getShooter();
                        if (shootingPlayer.getGameMode() == GameMode.CREATIVE
                            || shootingPlayer.getGameMode() == GameMode.SPECTATOR) {
                            return;
                        }
                        PlayerInventory inventory = shootingPlayer.getInventory();
                        boolean isMainHand = inventory.getItemInMainHand().getType() == Material.BOW
                            || inventory.getItemInMainHand().getType() == Material.CROSSBOW;
                        boolean isOffHand = inventory.getItemInOffHand().getType() == Material.BOW
                            || inventory.getItemInMainHand().getType() == Material.CROSSBOW;
                        ItemStack heldBow = null;
                        if (isMainHand) {
                            heldBow = inventory.getItemInMainHand();
                        }
                        if (isOffHand) {
                            heldBow = inventory.getItemInOffHand();
                        }

                        // if the player is holding a bow in both hands
                        // default to main hand since that takes priority
                        if (isMainHand && isOffHand) {
                            heldBow = inventory.getItemInMainHand();
                        }

                        // player swapped
                        if (heldBow == null) {
                            return;
                        }

                        // player is shooting another player
                        if (mobBeingAttacked instanceof Player) {
                            itemAddPlayerKill(heldBow);
                        } else {
                            itemAddMobKill(heldBow);
                        }
                    }
                }
            }
        });

        return true;
    }

    /**
     * Update the item count key. Returns the count or null.
     * 
     * @param tool
     */
    private Integer updateItemCount(ItemStack tool, NamespacedKey key, Integer increase) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return null;
        }

        Integer value = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.INTEGER)) {
            value = container.get(key, PersistentDataType.INTEGER);
            if (value == null) {
                value = 0;
            }
        }

        if (increase != 0) {
            value += increase;
            container.set(key, PersistentDataType.INTEGER, value);
            tool.setItemMeta(meta);
            SMItemLore.updateLore(tool);
        }

        return value;
    }

    /**
     * Get the item crafted at -data for this tool. Returns the date or null.
     * 
     * @param tool
     */
    private Long getItemCreatedAt(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Long timeCreated = container.get(itemCreated, PersistentDataType.LONG);
        return timeCreated;
    }

    /**
     * Set the item crafted at data for this tool.
     * 
     * @param tool
     */
    private void setItemCreatedAt(ItemStack tool) {
        if (!isTrackedItem(tool.getType())) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        long timeCreated = System.currentTimeMillis();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(itemCreated, PersistentDataType.LONG)) {
            return;
        }

        container.set(itemCreated, PersistentDataType.LONG, timeCreated);
        container.set(originType, PersistentDataType.INTEGER, 0);
        tool.setItemMeta(meta);
        SMItemLore.updateLore(tool);

        return;
    }

    /**
     * Get the item crafted by data for this tool. Returns the date or null.
     * 
     * @param tool
     */
    public Player getItemCreatedBy(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        UUID playerUUID = container.get(itemOwner, new SMPersistentUUIDDataType());
        if (playerUUID == null) {
            return null;
        }

        return Bukkit.getPlayer(playerUUID);
    }

    /**
     * Set the item crafted by data for this tool.
     * 
     * @param tool
     */
    private void setItemCreatedBy(ItemStack tool, Player player) {
        if (!isTrackedItem(tool.getType())) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(itemOwner, new SMPersistentUUIDDataType())) {
            return;
        }

        container.set(itemOwner, new SMPersistentUUIDDataType(), player.getUniqueId());
        tool.setItemMeta(meta);
        SMItemLore.updateLore(tool);

        return;
    }

    /**
     * Get the item crafted by data for this tool. Returns the date or null.
     * 
     * @param tool
     */
    public Integer itemGetPlayerKills(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer playerKillsCount = container.get(playerKills, PersistentDataType.INTEGER);

        return playerKillsCount;
    }

    /**
     * Set the item crafted by data for this tool.
     * 
     * @param tool
     */
    private void itemAddPlayerKill(ItemStack tool) {
        if (!isTrackedItem(tool.getType())) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        Integer playerKillCount = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(playerKills, PersistentDataType.INTEGER)) {
            playerKillCount = container.get(playerKills, PersistentDataType.INTEGER);
            if (playerKillCount == null) {
                playerKillCount = 0;
            }
        }

        playerKillCount++;
        container.set(playerKills, PersistentDataType.INTEGER, playerKillCount);
        tool.setItemMeta(meta);
        SMItemLore.updateLore(tool);

        return;
    }

    /**
     * Get the item crafted by data for this tool. Returns the date or null.
     * 
     * @param tool
     */
    public Integer itemGetMobKills(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer mobKillsCount = container.get(mobKills, PersistentDataType.INTEGER);

        return mobKillsCount;
    }

    /**
     * Set the item crafted by data for this tool.
     * 
     * @param tool
     */
    private void itemAddMobKill(ItemStack tool) {
        if (!isTrackedItem(tool.getType())) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        Integer mobKillCount = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(mobKills, PersistentDataType.INTEGER)) {
            mobKillCount = container.get(mobKills, PersistentDataType.INTEGER);
            if (mobKillCount == null) {
                mobKillCount = 0;
            }
        }

        mobKillCount++;
        container.set(mobKills, PersistentDataType.INTEGER, mobKillCount);
        tool.setItemMeta(meta);
        SMItemLore.updateLore(tool);

        return;
    }

    /**
     * Get the number of blocks mined with this tool
     * 
     * @param tool
     * @return
     */
    public Integer getBlocksMined(ItemStack tool) {
        return updateBlocksMined(tool, 0);
    }

    /**
     * Update the number of blocks mined by this tool by 1
     * 
     * @param tool
     */
    public void updateBlocksMined(ItemStack tool) {
        updateBlocksMined(tool, 1);
    }

    /**
     * Update the number of blocks mined by this tool. Returns the mined count or null.
     * 
     * @param tool
     */
    private Integer updateBlocksMined(ItemStack tool, Integer increase) {
        if (!isMiningTool(tool.getType())) {
            return null;
        }

        return updateItemCount(tool, blocksMined, increase);
    }

    /**
     * Get the number of crops mined with this tool
     * 
     * @param tool
     * @return
     */
    public Integer getCropsHarvested(ItemStack tool) {
        return updateCropsHarvested(tool, 0);
    }

    /**
     * Update the number of crops mined by this tool by 1
     * 
     * @param tool
     */
    public void updateCropsHarvested(ItemStack tool) {
        updateCropsHarvested(tool, 1);
    }

    /**
     * Update the number of blocks mined by this tool. Returns the mined count or null.
     * 
     * @param tool
     */
    private Integer updateCropsHarvested(ItemStack tool, Integer increase) {
        if (!isHarvestingTool(tool.getType())) {
            return null;
        }

        return updateItemCount(tool, cropsHarvested, increase);
    }

    /**
     * Get the number of fish caught with this tool
     * 
     * @param tool
     * @return
     */
    public Integer getFishCaught(ItemStack tool) {
        return updateFishCaught(tool, 0);
    }

    /**
     * Update the number of fish caught by this tool by 1
     * 
     * @param tool
     */
    public void updateFishCaught(ItemStack tool) {
        updateCropsHarvested(tool, 1);
    }

    /**
     * Update the number of fish caught by this tool. Returns the count or null.
     * 
     * @param tool
     */
    private Integer updateFishCaught(ItemStack tool, Integer increase) {
        if (!isFishingItem(tool.getType())) {
            return null;
        }

        return updateItemCount(tool, fishingRodCaught, increase);
    }

    /**
     * Get the number of arrows shot with this tool
     * 
     * @param tool
     * @return
     */
    public Integer getArrowsShot(ItemStack tool) {
        return updateArrowsShot(tool, 0);
    }

    /**
     * Update the number of arrows shot by this tool by 1
     * 
     * @param tool
     */
    public void updateArrowsShot(ItemStack tool) {
        updateArrowsShot(tool, 1);
    }

    /**
     * Update the number of arrows shot by this tool. Returns the count or null.
     * 
     * @param tool
     */
    private Integer updateArrowsShot(ItemStack tool, Integer increase) {
        if (!isBowItem(tool.getType())) {
            return null;
        }

        return updateItemCount(tool, arrowsShot, increase);
    }

    /**
     * Get the number of sheep sheared with this tool
     * 
     * @param tool
     * @return
     */
    public Integer getSheepSheared(ItemStack tool) {
        return updateSheepSheared(tool, 0);
    }

    /**
     * Update the number of sheep sheared by this tool by 1
     * 
     * @param tool
     */
    public void updateSheepSheared(ItemStack tool) {
        updateSheepSheared(tool, 1);
    }

    /**
     * Update the number of sheep sheared by this tool. Returns the count or null.
     * 
     * @param tool
     */
    private Integer updateSheepSheared(ItemStack tool, Integer increase) {
        if (!isShearingItem(tool.getType())) {
            return null;
        }

        return updateItemCount(tool, sheepSheared, increase);
    }

    /**
     * Is material a mining tool?
     * 
     * @param type
     * @return
     */
    public Boolean isMiningTool(Material type) {
        String lowerCase = type.toString().toLowerCase(Locale.ROOT);

        return (type == Material.SHEARS || lowerCase.contains("_pickaxe") || lowerCase.contains("_axe")
            || lowerCase.contains("_hoe") || lowerCase.contains("_shovel"));
    }

    /**
     * Is material a harvesting tool?
     * 
     * @param type
     * @return
     */
    public Boolean isHarvestingTool(Material type) {
        return type.toString().toLowerCase(Locale.ROOT).contains("_hoe");
    }

    /**
     * Is material an armor item
     * 
     * @param type
     * @return
     */
    public Boolean isArmor(Material type) {
        String lowerCase = type.toString().toLowerCase(Locale.ROOT);

        return (lowerCase.contains("_helmet") || lowerCase.contains("_chestplate") || lowerCase.contains("_leggings")
            || lowerCase.contains("_boots"));
    }

    /**
     * Is material a melee weapon
     * 
     * @param type
     * @return
     */
    public Boolean isMeleeWeapon(Material type) {
        String lowerCase = type.toString().toLowerCase(Locale.ROOT);

        return (type == Material.TRIDENT || lowerCase.contains("_sword") || lowerCase.contains("_axe"));
    }

    /**
     * Is material a fishing item
     * 
     * @param type
     * @return
     */
    public Boolean isFishingItem(Material type) {
        return type == Material.FISHING_ROD;
    }

    /**
     * Is material a bow item
     * 
     * @param type
     * @return
     */
    public Boolean isBowItem(Material type) {
        return type == Material.BOW || type == Material.CROSSBOW;
    }

    /**
     * Is material a shearing item
     * 
     * @param type
     * @return
     */
    public Boolean isShearingItem(Material type) {
        return type == Material.SHEARS;
    }

    /**
     * Is this material a type that is tracked?
     * 
     * @param type The material in question.
     * @return True if the material is a trackable type.
     */
    public Boolean isTrackedItem(Material type) {
        return (isMiningTool(type) || isHarvestingTool(type) || isArmor(type) || isMeleeWeapon(type)
            || isFishingItem(type) || isBowItem(type));
    }

    /**
     * Clear the tool stats data on this item.
     * 
     * @param tool The item to clear the stats.
     */
    public void clearStats(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(blocksMined);
        container.remove(cropsHarvested);
        container.remove(fishingRodCaught);
        container.remove(arrowsShot);
        container.remove(sheepSheared);
        container.remove(itemCreated);
        container.remove(itemOwner);
        container.remove(originType);
        container.remove(playerKills);
        container.remove(mobKills);

        tool.setItemMeta(meta);
        SMItemLore.updateLore(tool);
    }
}
