package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.config.SMConfigFile;
import java.util.stream.Collectors;

public class SMValue extends SMFeature {
    private static SMConfigFile valueConfig = null;
    private static Map<String, Float> denominationsMap = null;
    private static final int MAX_DENOMINATION_QTY = 64;
    private static final int MAX_PURCHASE_QTY = 64;

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {
        valueConfig = SMConfig.getOrLoadConfig("values.yml");
        denominationsMap = valueConfig.getFloatMap("denominations");

        // Sorting the denominationsMap based on value
        denominationsMap = denominationsMap.entrySet().stream()
            .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));

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
        String path = "values";

        if (valueConfig.contains(path + "." + item)) {
            return valueConfig.getFloat(path + "." + item);
        } else if (!item.contains(":")) {
            if (valueConfig.contains(path + ".minecraft:" + item)) {
                return valueConfig.getFloat(path + ".minecraft:" + item);
            }
        }

        return 0f;
    }

    /**
     * This class represents the result of the denomination calculation.
     */
    public static class Result {
        public HashMap<String, Integer> denominations;
        public int purchasingItemQuantity;
        public float finalValue;

        public Result() {
            this.purchasingItemQuantity = 0;
            this.denominations = new HashMap<>();
        }

        // Constructor
        public Result(int purchasingItemQuantity, HashMap<String, Integer> denominations) {
            this.purchasingItemQuantity = purchasingItemQuantity;
            this.denominations = denominations;
        }

        @Override
        public String toString() {
            return "Purchasing Item Quantity: " + purchasingItemQuantity + ", Denominations: " + denominations
                + ", Final Value: $" + finalValue;
        }
    }

    /**
     * Calculate the denominations for a given purchasing item value.
     * 
     * @param purchasingItemValue The value of the purchasing item.
     * @return The result with the denominations.
     */
    public static Result calculateDenominations(float purchasingItemValue) {
        List<Map.Entry<String, Float>> sortedItems = new ArrayList<>(denominationsMap.entrySet());

        sortedItems.sort(Map.Entry.<String, Float>comparingByValue());

        int purchasingItemQty = 1;
        float targetValue = purchasingItemQty * purchasingItemValue;
        float bestDifference = Float.MAX_VALUE;

        sortedItems.sort(Map.Entry.<String, Float>comparingByValue().reversed());

        HashMap<String, Integer> bestSolution = new HashMap<>();
        float bestValue = 0.0f;

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
                                bestValue = totalValue;
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
                targetValue = purchasingItemQty * purchasingItemValue;
            }
        }

        Result result = new Result();
        result.denominations = bestSolution;
        result.purchasingItemQuantity = purchasingItemQty;
        result.finalValue = bestValue;
        return result;
    }

    /**
     * Calculate the single denomination for a given target value.
     * 
     * @param targetValue The target value to find the denomination for.
     * @return The result with the denomination.
     */
    public static Result calculateSingleDenomination(float targetValue) {
        List<String> sortedItems = denominationsMap.keySet().stream()
            .sorted((k1, k2) -> Float.compare(denominationsMap.get(k2), denominationsMap.get(k1)))
            .collect(Collectors.toList());

        Result bestResult = new Result();
        float closestDifference = Float.MAX_VALUE;

        for (int purchaseQty = 1; purchaseQty <= MAX_PURCHASE_QTY; purchaseQty++) {
            float adjustedTargetValue = targetValue * purchaseQty;

            for (String item : sortedItems) {
                float itemValue = denominationsMap.get(item);
                int itemQty = (int) (adjustedTargetValue / itemValue);

                if (itemQty <= MAX_DENOMINATION_QTY) {
                    float totalValue = itemQty * itemValue;
                    float difference = adjustedTargetValue - totalValue;

                    if (difference < closestDifference && difference >= 0) {
                        bestResult.purchasingItemQuantity = purchaseQty;
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
}
