package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;

public class SMClearInventory extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        new SMCommand("clearinventory")
            .alias("clearinv", "ci")
            .tabComplete("{player}")
            .permission("stemcraft.inventory.clear")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);

                // Require player arg from console
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length == 0), "CMD_PLAYER_REQ_FROM_CONSOLE");
                
                // Check targetPlayer is not null
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                
                // Check sender has permission to target another player
                ctx.checkBooleanLocale(
                    targetPlayer == ctx.player || ctx.hasPermission("stemcraft.inventory.clear.other"),
                    "CMD_NO_PERMISSION"
                );

                PlayerInventory inventory = targetPlayer.getInventory();

                // Save inventory if GameModeInventories feature is enabled
                if(STEMCraft.featureEnabled("GameModeInventories")) {
                    SMGameModeInventories gmiInventories = STEMCraft.getFeature("SMGameModeInventories", SMGameModeInventories.class);
                    gmiInventories.SaveInventory(targetPlayer, "Player cleared inventory");
                }

                inventory.clear();
                inventory.setArmorContents(null);

                if(targetPlayer == ctx.sender) {
                    ctx.returnInfoLocale("INV_CLEARED");
                } else {
                    SMMessenger.infoLocale(ctx.sender, "INV_CLEARED_FOR", "player", targetPlayer.getName());
                    SMMessenger.infoLocale(targetPlayer, "INV_CLEARED_BY", "player", ctx.senderName());
                }
            })
            .register();
        return true;
    }
}
