package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;

public class SMRepair extends SMFeature {

    @Override
    protected Boolean onEnable() {
        String[] repairTypes = {"hand", "all"};

        new SMCommand("repair")
            .permission("stemcraft.inventory.repair")
            .tabComplete(repairTypes, "{player}")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(2, ctx.player);
                Boolean all = false;

                if (ctx.fromConsole()) {
                    ctx.checkArgsLocale(2, "CMD_PLAYER_REQ_FROM_CONSOLE");
                }

                if (ctx.args.size() > 0) {
                    ctx.checkInArrayLocale(repairTypes, ctx.args.get(0), "REPAIR_USAGE");
                    all = ctx.args.get(0).toLowerCase() == "all";
                }

                if (ctx.args.size() > 1) {
                    ctx.checkPermission("stemcraft.inventory.repair.other");
                }

                PlayerInventory inventory = targetPlayer.getInventory();
                if (all) {
                    ItemStack[] items = inventory.getContents();

                    for (ItemStack item : items) {
                        if (item != null && SMCommon.itemIsRepairable(item) == true) {
                            item = SMCommon.repairItem(item);
                        }
                    }

                    ctx.returnInfoLocale("REPAIR_ALL_REPAIRED");
                } else {
                    targetPlayer.updateInventory();
                    ItemStack item = inventory.getItemInMainHand();
                    if (SMCommon.itemIsRepairable(item) == true) {
                        item = SMCommon.repairItem(item);
                        inventory.setItemInMainHand(item);
                        ctx.returnInfoLocale("REPAIR_HAND_REPAIRED");
                    } else {
                        ctx.returnInfoLocale("REPAIR_NOT_REPAIRABLE");
                    }
                }
            })
            .register();

        return true;
    }

}
