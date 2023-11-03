package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.BookMeta;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMJson;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.feature.SMValue.Result;

/**
 * A trader villager that spawns around the world.
 */
public class SMTrader extends SMFeature {
    private static Villager trader = null;

    private static Integer DELAY_TRADE_BETWEEN_WORLDS = 60 * 60 * 4; // 4 hour delay for trade to show between worlds
    private static Double DELAY_TRADE_BY_BLOCKS = 0.6; // the time in secs to apply to the delay for each block away
    private static Integer LESS_THAN_SECONDS_TO_CALCULATE = 60 * 60 * 24; // If less then this amount of seconds have
                                                                          // passed, delay the trade

    private static String TRADER_WORLD = "survival";
    private static Integer UPDATE_INTERVAL = 20 * 60 * 5;
    private static Double DESPAWN_DISTANCE = 64d;
    private static Double SPAWN_CHANCE = 0.02d;

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {
        SMDatabase.runMigration("231103184700_CreateTraderTable", () -> {
            SMDatabase.prepareStatement(
                "CREATE TABLE IF NOT EXISTS trades (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "material TEXT NOT NULL," +
                    "quantity NUMBER NOT NULL," +
                    "location TEXT NOT NULL," +
                    "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
                .executeUpdate();
        });

        new SMCommand("trader")
            .permission("stemcraft.command.trader")
            .action(ctx -> {
                ctx.checkNotConsole();

                spawnTrader(ctx.player.getLocation());
            }).register();

        SMEvent.register(PlayerInteractEntityEvent.class, ctx -> {
            if (ctx.event.getRightClicked() instanceof Villager && ctx.event.getRightClicked() == trader
                && trader != null) {

                Player player = ctx.event.getPlayer();
                List<MerchantRecipe> trades = new ArrayList<>();
                Set<String> playerItems = new HashSet<>();
                Set<String> denominations = new HashSet<>();

                // Add player inventory items
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        String itemName = SMBridge.getMaterialName(item);

                        if (!playerItems.contains(itemName)) {
                            playerItems.add(itemName);

                            MerchantRecipe trade = addPlayerSell(item);
                            if (trade != null) {
                                trades.add(trade);
                            }

                            if (SMValue.getDenominations().contains(itemName)
                                && SMCommon.totalItemType(player.getInventory(), itemName) > 8) {
                                String prevDenomination =
                                    SMCommon.getPrevListItem(SMValue.getDenominations(), itemName);
                                if (prevDenomination != null) {
                                    ItemStack prevDenominationItem = SMBridge.newItemStack(prevDenomination);
                                    trade = addPlayerBuy(prevDenominationItem);
                                    if (trade != null) {
                                        trades.add(trade);
                                    }
                                }
                            }
                        }
                    }
                }

                // Add trader items
                Map<String, Integer> items = GetItemsToTrade(player.getLocation());
                for (String material : items.keySet()) {
                    ItemStack itemStack = SMBridge.newItemStack(material, 1);
                    if (itemStack != null) {
                        Integer available = items.get(material);
                        MerchantRecipe trade = addPlayerBuy(itemStack);

                        List<ItemStack> ingredients = trade.getIngredients();
                        for (ItemStack ingredient : ingredients) {
                            String ingredientName = SMBridge.getMaterialName(ingredient);
                            if (ingredientName != null) {
                                denominations.add(ingredientName);
                            }
                        }

                        if (trade.getResult().getAmount() <= available) {
                            trade.setMaxUses((int) Math.floor(available / trade.getResult().getAmount()));
                            if (trade != null) {
                                trades.add(trade);
                            }
                        }
                    }
                }

                // Add denomination trading
                for (String denomination : denominations) {
                    ItemStack itemStack = SMBridge.newItemStack(denomination, 1);
                    MerchantRecipe trade = addPlayerBuy(itemStack);
                    if (trade != null) {
                        trades.add(trade);
                    }
                }

                trader.setRecipes(trades);
            }

        });

        SMEvent.register(InventoryCloseEvent.class, ctx -> {
            if (ctx.event.getInventory() instanceof MerchantInventory) {
                MerchantInventory merchantInventory = (MerchantInventory) ctx.event.getInventory();
                HumanEntity player = ctx.event.getPlayer();
                Location location = player.getLocation();

                merchantInventory.getMerchant().getRecipes().forEach(recipe -> {
                    ItemStack result = recipe.getResult();
                    List<ItemStack> ingredients = recipe.getIngredients();

                    String resultName = SMBridge.getMaterialName(result);
                    ItemStack ingredientItem = ingredients.get(0);
                    String ingredientName = SMBridge.getMaterialName(ingredientItem);

                    if (recipe.getUses() > 0) {
                        if (SMValue.getDenominations().contains(resultName)) {
                            // player sold an item
                            AddItemToTrader(ingredientName, recipe.getUses() * ingredientItem.getAmount(), location);
                        } else {
                            // player bought an item
                            RemoveItemFromTrader(resultName, recipe.getUses() * result.getAmount(), location);
                        }
                    }
                });
            }
        });


        // STEMCraft.runTimer(UPDATE_INTERVAL, () -> {
        // if (trader == null) {
        // Random random = new Random();

        // double chance = random.nextDouble();
        // if (chance < (SPAWN_CHANCE / 100)) {
        // // player on surface
        // // daytime
        // }
        // } else {
        // Boolean visible = false;

        // for (Player player : Bukkit.getServer().getOnlinePlayers()) {
        // // Check if the player and the entity are in the same world
        // if (player.getWorld().getName().equals(TRADER_WORLD)) {
        // double distance = player.getLocation().distance(trader.getLocation());

        // if (distance <= DESPAWN_DISTANCE) {
        // visible = true;
        // break;
        // }
        // }
        // }

        // if (!visible) {
        // trader.remove();
        // trader = null;
        // }
        // }
        // });

        return true;
    }

    /**
     * Called when the feature is requested to be disabled.
     */
    @Override
    protected void onDisable() {
        removeTrader();
    }


    private static void spawnTrader(Location location) {
        removeTrader();

        trader = location.getWorld().spawn(location, Villager.class);
        trader.setCustomName("Trader");
        trader.setCustomNameVisible(true);
        trader.setRemoveWhenFarAway(true);
        trader.setProfession(Profession.LEATHERWORKER);
        trader.setVillagerLevel(5);
    }

    private static void removeTrader() {
        if (trader != null) {
            trader.remove();
            trader = null;
        }
    }

    private static Boolean traderActive() {
        if (trader != null) {
            if (trader.isValid()) {
                return true;
            }

            trader = null;
        }

        return false;
    }

    private static MerchantRecipe addPlayerBuy(ItemStack item) {
        String itemName = SMBridge.getMaterialName(item);
        Float value = SMValue.getValue(itemName);
        if (value > 0f) {
            Result denominationResult = SMValue.calculateDenominations(value, itemName);
            List<ItemStack> ingredients = denominationResult.toItemStacks();
            if (ingredients.size() > 0) {
                ItemStack buyItem = SMBridge.newItemStack(itemName, denominationResult.quantity);
                MerchantRecipe trade = new MerchantRecipe(buyItem, 9999);
                trade.addIngredient(ingredients.get(0));
                if (ingredients.size() > 1) {
                    trade.addIngredient(ingredients.get(1));
                }
                return trade;
            }
        }

        return null;
    }

    private static MerchantRecipe addPlayerSell(ItemStack item) {
        String itemName = SMBridge.getMaterialName(item);
        Float value = SMValue.getValue(itemName);
        if (value > 0f) {
            value *= 0.9f;
            Result denominationResult = SMValue.calculateSingleDenomination(value, itemName);
            List<ItemStack> ingredients = denominationResult.toItemStacks();
            if (ingredients.size() > 0) {
                ItemStack sellItem = SMBridge.newItemStack(itemName, denominationResult.quantity);
                MerchantRecipe trade = new MerchantRecipe(ingredients.get(0), 9999);
                trade.addIngredient(sellItem);
                return trade;
            }
        }

        return null;
    }

    private static Map<String, Integer> GetItemsToTrade(Location location) {
        Map<String, Integer> itemMap = new HashMap<>();

        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT * FROM trades WHERE 1");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String material = resultSet.getString("material");
                Integer quantity = resultSet.getInt("quantity");
                String itemLocationJson = resultSet.getString("location");
                String created = resultSet.getString("created");
                Location itemLocation = SMJson.fromJson(Location.class, itemLocationJson);

                Date createdDate = SMDatabase.DATE_FORMAT.parse(created);
                long createdMillis = createdDate.getTime();
                long secondsSinceCreated = (System.currentTimeMillis() - createdMillis) / 1000;

                if (secondsSinceCreated < LESS_THAN_SECONDS_TO_CALCULATE) {
                    if (location.getWorld().getName().equals(itemLocation.getWorld().getName())) {
                        Double distance = location.distance(itemLocation);
                        if (secondsSinceCreated < distance * DELAY_TRADE_BY_BLOCKS) {
                            continue;
                        }
                    } else {
                        if (secondsSinceCreated < DELAY_TRADE_BETWEEN_WORLDS) {
                            continue;
                        }
                    }
                }

                itemMap.put(material, itemMap.getOrDefault(material, 0) + quantity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemMap;
    }

    private static void AddItemToTrader(String material, Integer quantity, Location location) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "INSERT INTO trades (material, quantity, location) VALUES (?, ?, ?)");
            statement.setString(1, material);
            statement.setInt(2, quantity);
            statement.setString(3, SMJson.toJson(location, Location.class));

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void RemoveItemFromTrader(String material, Integer quantity, Location location) {
        Integer remaining = quantity;

        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT * FROM trades WHERE material = ? ORDER BY created");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Integer itemId = resultSet.getInt("id");
                Integer itemQuantity = resultSet.getInt("quantity");

                if (itemQuantity > remaining) {
                    PreparedStatement updateStatement = SMDatabase.prepareStatement(
                        "UPDATE trades SET quantity = ? WHERE id = ?");
                    updateStatement.setInt(1, itemQuantity - remaining);
                    updateStatement.executeUpdate();
                    break;
                } else {
                    PreparedStatement deleteStatement = SMDatabase.prepareStatement(
                        "DELETE FROM trades WHERE id = ?");
                    deleteStatement.setInt(1, itemId);
                    deleteStatement.executeUpdate();
                }

                remaining -= itemQuantity;
                if (remaining <= 0) {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
