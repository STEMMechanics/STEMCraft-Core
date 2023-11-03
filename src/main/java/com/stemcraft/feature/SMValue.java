package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.config.SMConfigFile;
import java.util.stream.Collectors;

public class SMValue extends SMFeature {
    private static SMConfigFile valueConfig = null;
    private static Map<String, Float> denominationsMap = null;
    private static final int MAX_STACK_SIZE = 64;

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {
        valueConfig = SMConfig.getOrLoadConfig("values.yml");
        denominationsMap = null;
        denominationsMap = valueConfig.getFloatMap("denominations");

        // Sorting the denominationsMap based on value
        denominationsMap = denominationsMap.entrySet().stream()
            .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        new SMCommand("value")
            .permission("stemcraft.command.value")
            .tabComplete("{material}")
            .action(ctx -> {
                String rawName = String.join(" ", ctx.args).toLowerCase();
                ItemStack item = SMBridge.newItemStack(rawName);
                if (item == null) {
                    rawName = rawName.replace(" ", "_");
                    item = SMBridge.newItemStack(rawName);
                    if (item == null) {
                        ctx.returnErrorLocale("VALUE_ITEM_NOT_FOUND");
                    }
                }

                String itemName = SMBridge.getMaterialName(item);
                Float itemValue = getValue(itemName);
                if (itemValue <= 0f) {
                    ctx.returnErrorLocale("VALUE_ITEM_NO_VALUE");
                }

                Result result = calculateDenominations(itemValue, itemName);
                if (result.quantity == 0 || result.denominations.size() == 0) {
                    ctx.returnErrorLocale("VALUE_ITEM_NO_VALUE");
                }

                String valueString =
                    result.quantity + " "
                        + SMCommon.pluralize(SMBridge.getMaterialDisplayName(SMBridge.newItemStack(itemName)),
                            result.quantity)
                        + " is worth ";

                int index = 0;
                int size = result.denominations.size();

                for (String materialName : result.denominations.keySet()) {
                    int amount = result.denominations.get(materialName);
                    String displayName = SMBridge.getMaterialDisplayName(SMBridge.newItemStack(materialName));

                    valueString += amount + " " + SMCommon.pluralize(displayName, amount);

                    if (index < size - 2) {
                        valueString += ", ";
                    } else if (index == size - 2) {
                        valueString += " and ";
                    }

                    index++;
                }

                ctx.returnInfo(valueString);
            })
            .register();

        return true;
    }

    /**
     * Get the value of a material.
     * 
     * @param material The material to get its value.
     * @return The value of the material.
     */
    public static Float getValue(Material material) {
        return getValue(material.name());
    }

    /**
     * Get the value of an item.
     * 
     * @param item The item to get its value.
     * @return The value of the item.
     */
    public static Float getValue(String item) {
        Float value = getValue(item, "values");
        if (value != null) {
            return value;
        }

        value = getValue(item, "denominations");
        if (value != null) {
            return value;
        }

        return 0f;
    }

    private static Float getValue(String item, String path) {
        item = item.toLowerCase();
        if (valueConfig.contains(path + "." + item)) {
            return valueConfig.getFloat(path + "." + item);
        } else if (!item.contains(":")) {
            if (valueConfig.contains(path + ".minecraft:" + item)) {
                return valueConfig.getFloat(path + ".minecraft:" + item);
            }
        } else if (item.startsWith("minecraft:")) {
            String minecraftItem = item.substring(10);
            if (valueConfig.contains(path + "." + minecraftItem)) {
                return valueConfig.getFloat(path + "." + minecraftItem);
            }
        }

        return null;
    }

    /**
     * This class represents the result of the denomination calculation.
     */
    public static class Result {
        public HashMap<String, Integer> denominations;
        public int quantity;

        public Result() {
            this.quantity = 0;
            this.denominations = new HashMap<>();
        }

        // Constructor
        public Result(int quantity, HashMap<String, Integer> denominations) {
            this.quantity = quantity;
            this.denominations = denominations;
        }

        public List<ItemStack> toItemStacks() {
            List<ItemStack> items = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : denominations.entrySet()) {
                ItemStack itemStack = SMBridge.newItemStack(entry.getKey(), entry.getValue());
                items.add(itemStack);
            }

            return items;
        }
    }

    /**
     * Calculate the denominations for a given purchasing item value.
     * 
     * @param value The value of the purchasing item.
     * @return The result with the denominations.
     */
    public static Result calculateDenominations(float value) {
        return calculateDenominations(value, null);
    }

    /**
     * Calculate the denominations for a given purchasing item value.
     * 
     * @param value The value of the purchasing item.
     * @param ignore The denomination name that should be ignored or null.
     * @return The result with the denominations.
     */
    public static Result calculateDenominations(float value, String ignore) {
        List<Map.Entry<String, Float>> sortedItems = new ArrayList<>(denominationsMap.entrySet());

        if (ignore != null) {
            sortedItems.removeIf(entry -> entry.getKey().equals(ignore));
        }

        sortedItems.sort(Map.Entry.<String, Float>comparingByValue());

        int purchasingItemQty = 1;
        float targetValue = purchasingItemQty * value;
        float bestDifference = Float.MAX_VALUE;

        sortedItems.sort(Map.Entry.<String, Float>comparingByValue().reversed());

        HashMap<String, Integer> bestSolution = new HashMap<>();

        while (bestDifference != 0.0f && purchasingItemQty < 64) {
            for (Map.Entry<String, Float> entry1 : sortedItems) {
                for (Map.Entry<String, Float> entry2 : sortedItems) {
                    int maxForEntry1 = (int) Math.min(targetValue / entry1.getValue(), 64);
                    int maxForEntry2 =
                        (int) Math.min((targetValue - maxForEntry1 * entry1.getValue()) / entry2.getValue(), 64);

                    for (int qty1 = 0; qty1 <= maxForEntry1; qty1++) {
                        for (int qty2 = 0; qty2 <= maxForEntry2; qty2++) {
                            float totalValue = qty1 * entry1.getValue() + qty2 * entry2.getValue();

                            if (totalValue <= targetValue && Math.abs(totalValue - targetValue) < bestDifference) {
                                bestDifference = Math.abs(totalValue - targetValue);
                                bestSolution.clear();
                                if (qty1 > 0)
                                    bestSolution.put(entry1.getKey(), qty1);
                                if (qty2 > 0)
                                    bestSolution.put(entry2.getKey(), qty2);
                            }
                        }
                    }
                }
            }

            if (bestDifference != 0.0f) {
                purchasingItemQty++;
                targetValue = purchasingItemQty * value;
            }
        }

        Result result = new Result();
        result.denominations = bestSolution;
        result.quantity = purchasingItemQty;
        return result;
    }

    /**
     * Calculate the single denomination for a given target value.
     * 
     * @param targetValue The target value to find the denomination for.
     * @return The result with the denomination.
     */
    public static Result calculateSingleDenomination(float targetValue) {
        return calculateSingleDenomination(targetValue, null);
    }

    /**
     * Calculate the single denomination for a given target value.
     * 
     * @param targetValue The target value to find the denomination for.
     * @param ignore The denomination name that should be ignored or null.
     * @return The result with the denomination.
     */
    public static Result calculateSingleDenomination(float targetValue, String ignore) {
        List<String> sortedItems = denominationsMap.keySet().stream()
            .sorted((k1, k2) -> Float.compare(denominationsMap.get(k2), denominationsMap.get(k1)))
            .collect(Collectors.toList());

        if (ignore != null) {
            sortedItems.remove(ignore);
        }

        Result bestResult = new Result();
        float closestDifference = Float.MAX_VALUE;

        for (int purchaseQty = 1; purchaseQty <= MAX_STACK_SIZE; purchaseQty++) {
            float adjustedTargetValue = targetValue * purchaseQty;

            for (String item : sortedItems) {
                float itemValue = denominationsMap.get(item);
                int itemQty = (int) (adjustedTargetValue / itemValue);

                if (itemQty <= MAX_STACK_SIZE) {
                    float totalValue = itemQty * itemValue;
                    float difference = adjustedTargetValue - totalValue;

                    if (difference < closestDifference && difference >= 0) {
                        bestResult.quantity = purchaseQty;
                        bestResult.denominations.clear();
                        bestResult.denominations.put(item, itemQty);

                        if (difference == 0) {
                            return bestResult;
                        }

                        closestDifference = difference;
                    }
                }
            }
        }

        return bestResult;
    }

    /**
     * Return a list of denomination material names.
     * 
     * @return A list of material names.
     */
    public static List<String> getDenominations() {
        return SMCommon.setToList(denominationsMap.keySet());
    }
}
