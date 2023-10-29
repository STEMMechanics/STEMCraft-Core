package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;

/**
 * Allows the player to add/remove enchantments from an item.
 */
public class SMEnchant extends SMFeature {

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {

        SMTabComplete.register("enchantment", () -> {
            List<String> completions = new ArrayList<>();
            for (Enchantment enchantment : Enchantment.values()) {
                completions.add(enchantment.getKey().getKey());
            }

            return completions;
        });

        SMTabComplete.register("enchantment_level", () -> {
            List<String> levels = Arrays.asList("1", "2", "3", "4", "5");
            return levels;
        });

        new SMCommand("enchant")
            .tabComplete("add", "{enchantment}", "{enchantment_level}", "{player}")
            .tabComplete("remove", "{enchantment}", "{player}")
            .tabComplete("removeall", "{player}")
            .permission("stemcraft.command.enchant")
            .action(ctx -> {
                // Check there are args
                ctx.checkArgsLocale(1, "ENCHANT_USAGE");
                String sub = ctx.args[0].toLowerCase();

                // Sub command - add
                if ("add".equals(sub)) {
                    ctx.checkArgsLocale(2, "ENCHANT_USAGE_ADD");

                    Player targetPlayer = ctx.getArgAsPlayer(3, ctx.player);
                    ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                    ctx.checkPermission(
                        ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                        "stemcraft.command.enchant.other");

                    ItemStack item = targetPlayer.getInventory().getItemInMainHand();

                    Enchantment enchantment = SMBridge.getEnchantment(ctx.args[1]);
                    if (enchantment == null) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_NAME");
                    }

                    int level = 1;
                    try {
                        level = Integer.parseInt(ctx.args[2]);
                    } catch (NumberFormatException e) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_LEVEL");
                    }
                    ctx.checkBooleanLocale(level < 1, "ENCHANT_INVALID_LEVEL");

                    if (item == null || item.getType() == Material.AIR) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_ITEM");
                    } else {
                        item.addUnsafeEnchantment(enchantment, level);
                    }

                    if (!targetPlayer.getUniqueId().equals(ctx.player.getUniqueId())) {
                        SMMessenger.successLocale(targetPlayer, "ENCHANT_ADDED");
                    }

                    ctx.returnSuccessLocale("ENCHANT_ADDED");

                    // Sub command - remove
                } else if ("remove".equals(sub)) {
                    ctx.checkArgsLocale(2, "ENCHANT_USAGE_ADD");

                    Player targetPlayer = ctx.getArgAsPlayer(2, ctx.player);
                    ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                    ctx.checkPermission(
                        ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                        "stemcraft.command.enchant.other");

                    ItemStack item = targetPlayer.getInventory().getItemInMainHand();

                    Enchantment enchantment = SMBridge.getEnchantment(ctx.args[1]);
                    if (enchantment == null) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_NAME");
                    }

                    if (item == null || item.getType() == Material.AIR) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_ITEM");
                    } else {
                        item.removeEnchantment(enchantment);
                    }

                    if (!targetPlayer.getUniqueId().equals(ctx.player.getUniqueId())) {
                        SMMessenger.successLocale(targetPlayer, "ENCHANT_REMOVED");
                    }

                    ctx.returnSuccessLocale("ENCHANT_REMOVED");

                    // Sub command - removeall
                } else if ("removeall".equals(sub)) {
                    ctx.checkArgsLocale(2, "ENCHANT_USAGE_ADD");

                    Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                    ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                    ctx.checkPermission(
                        ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                        "stemcraft.command.enchant.other");

                    ItemStack item = targetPlayer.getInventory().getItemInMainHand();
                    if (item == null || item.getType() == Material.AIR) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_ITEM");
                    } else {
                        item.getEnchantments().forEach((enchantment, level) -> {
                            item.removeEnchantment(enchantment);
                        });
                    }

                    if (!targetPlayer.getUniqueId().equals(ctx.player.getUniqueId())) {
                        SMMessenger.successLocale(targetPlayer, "ENCHANT_REMOVED_ALL");
                    }

                    ctx.returnSuccessLocale("ENCHANT_REMOVED_ALL");
                } else {
                    ctx.returnErrorLocale("ENCHANT_USAGE");
                }
            })
            .register();

        return true;
    }
}
