package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;

/**
 * Allows the creation of custom books that can be saved and shown to the player using a command
 */
public class SMEnchant extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {

        SMTabComplete.register("enchantment", () -> {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                for (Enchantment enchantment : Enchantment.values()) {
                    completions.add(enchantment.getKey().getKey());
                }
            }

            return completions;
        });

        SMTabComplete.register("enchantment_level", () -> {
            List<String> levels = Arrays.asList("1", "2", "3", "4", "5");

            return levels;
        });

        new SMCommand("enchant")
            .tabComplete("add", "{enchantment}", "{enchantment_level}", "{player}")
            .tabComplete("remove" "{enchantment}", "{player}")
            .tabComplete("removeall", "{player}")
            .permission("stemcraft.command.enchant")
            .action(ctx -> {
                // enchant add <enchantment> <level> [<player>]
                // enchant remove <enchantment> [<player>]
                // enchant removeall [<player>]

                Player targetPlayer = null;
                Enchantment enchantment = null;
                int level = 0;

                // Check there are args
                ctx.checkArgsLocale(1, "ENCHANT_USAGE");

                String sub = ctx.args[0].toLowerCase();

                // Sub command - add
                if("add".equals(sub)) {
                    ctx.checkArgsLocale(2, "ENCHANT_USAGE_ADD");

                    Player targetPlayer = ctx.getArgAsPlayer(3, ctx.player);
                    ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                    ctx.checkPermission(ctx.isConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()), "stemcraft.command.enchant.other");

                    ItemStack item = targetPlayer.getItemInHand();
                    if (item == null || item.getType() == Material.AIR) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_ITEM");
                        return true;
                    }

                    Enchantment enchantment = Enchantment.getByKey(Enchantment.key(ctx.args[1]));
                    if (enchantment == null) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_NAME");
                    }

                    int level;
                    try {
                        level = Integer.parseInt(ctx.args[2]);
                    } catch (NumberFormatException e) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_LEVEL");
                    }
                    ctx.checkBooleanLocale(level < 1, "ENCHANT_INVALID_LEVEL");

                    item.addUnsafeEnchantment(enchantment, level);
           
                    if(!targetPlayer.getUniqueId().equals(ctx.player.getUniqueId())) {
                        SMMessenger.successLocale(targetPlayer, "ENCHANT_ADDED");
                    }

                    ctx.returnSuccessLocale("ENCHANT_ADDED");
                
                // Sub command - remove
                } else if("remove".equals(sub)) {
                    ctx.checkArgsLocale(2, "ENCHANT_USAGE_ADD");

                    Player targetPlayer = ctx.getArgAsPlayer(2, ctx.player);
                    ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                    ctx.checkPermission(ctx.isConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()), "stemcraft.command.enchant.other");

                    ItemStack item = targetPlayer.getItemInHand();
                    if (item == null || item.getType() == Material.AIR) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_ITEM");
                        return true;
                    }

                    Enchantment enchantment = Enchantment.getByKey(Enchantment.key(ctx.args[1]));
                    if (enchantment == null) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_NAME");
                    }

                    item.removeEnchantment(enchantment);
           
                    if(!targetPlayer.getUniqueId().equals(ctx.player.getUniqueId())) {
                        SMMessenger.successLocale(targetPlayer, "ENCHANT_REMOVED");
                    }

                    ctx.returnSuccessLocale("ENCHANT_REMOVED");

                // Sub command - removeall
                } else if("removeall".equals(sub)) {
                    ctx.checkArgsLocale(2, "ENCHANT_USAGE_ADD");

                    Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                    ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                    ctx.checkPermission(ctx.isConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()), "stemcraft.command.enchant.other");

                    ItemStack item = targetPlayer.getItemInHand();
                    if (item == null || item.getType() == Material.AIR) {
                        ctx.returnErrorLocale("ENCHANT_INVALID_ITEM");
                        return true;
                    }

                    item.getEnchantments().forEach((enchantment, level) ->
                        item.removeEnchantment(enchantment);
                    );
           
                    if(!targetPlayer.getUniqueId().equals(ctx.player.getUniqueId())) {
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
