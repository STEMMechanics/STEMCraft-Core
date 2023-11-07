package com.stemcraft.feature;

import org.bukkit.entity.Player;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;

/**
 * Allows the player to open workbenches on command
 */
public class SMWorkbench extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        new SMCommand("workbench")
            .tabComplete("{player}")
            .permission("stemcraft.command.workbench")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                ctx.checkPermission(ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.workbench.other");

                targetPlayer.openWorkbench(null, true);
            })
            .register();

        new SMCommand("anvil")
            .tabComplete("{player}")
            .permission("stemcraft.command.anvil")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                ctx.checkPermission(ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.anvil.other");

                SMBridge.openAnvil(targetPlayer, null, true);
            })
            .register();

        new SMCommand("cartographytable")
            .tabComplete("{player}")
            .permission("stemcraft.command.cartographytable")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                ctx.checkPermission(ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.cartographytable.other");

                SMBridge.openCartographyTable(targetPlayer, null, true);
            })
            .register();

        new SMCommand("grindstone")
            .tabComplete("{player}")
            .permission("stemcraft.command.grindstone")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                ctx.checkPermission(ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.grindstone.other");

                SMBridge.openGrindstone(targetPlayer, null, true);
            })
            .register();

        new SMCommand("loom")
            .tabComplete("{player}")
            .permission("stemcraft.command.loom")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                ctx.checkPermission(ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.loom.other");

                SMBridge.openLoom(targetPlayer, null, true);
            })
            .register();

        new SMCommand("smithingtable")
            .tabComplete("{player}")
            .permission("stemcraft.command.smithingtable")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                ctx.checkPermission(ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.smithingtable.other");

                SMBridge.openSmithingTable(targetPlayer, null, true);
            })
            .register();

        new SMCommand("stonecutter")
            .tabComplete("{player}")
            .permission("stemcraft.command.stonecutter")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                ctx.checkPermission(ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.stonecutter.other");

                SMBridge.openStonecutter(targetPlayer, null, true);
            })
            .register();

        return true;
    }
}
