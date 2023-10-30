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
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");
                ctx.checkPermission(ctx.args.length == 0, "stemcraft.command.workbench.other");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                targetPlayer.openWorkbench(null, true);
            })
            .register();

        new SMCommand("anvil")
            .tabComplete("{player}")
            .permission("stemcraft.command.anvil")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");
                ctx.checkPermission(ctx.args.length == 0, "stemcraft.command.anvil.other");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                SMBridge.openAnvil(targetPlayer, null, true);
            })
            .register();

        new SMCommand("cartographytable")
            .tabComplete("{player}")
            .permission("stemcraft.command.cartographytable")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");
                ctx.checkPermission(ctx.args.length == 0, "stemcraft.command.cartographytable.other");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                SMBridge.openCartographyTable(targetPlayer, null, true);
            })
            .register();

        new SMCommand("grindstone")
            .tabComplete("{player}")
            .permission("stemcraft.command.grindstone")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");
                ctx.checkPermission(ctx.args.length == 0, "stemcraft.command.grindstone.other");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                SMBridge.openGrindstone(targetPlayer, null, true);
            })
            .register();

        new SMCommand("loom")
            .tabComplete("{player}")
            .permission("stemcraft.command.loom")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");
                ctx.checkPermission(ctx.args.length == 0, "stemcraft.command.loom.other");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                SMBridge.openLoom(targetPlayer, null, true);
            })
            .register();

        new SMCommand("smithingtable")
            .tabComplete("{player}")
            .permission("stemcraft.command.smithingtable")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");
                ctx.checkPermission(ctx.args.length == 0, "stemcraft.command.smithingtable.other");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                SMBridge.openSmithingTable(targetPlayer, null, true);
            })
            .register();

        new SMCommand("stonecutter")
            .tabComplete("{player}")
            .permission("stemcraft.command.stonecutter")
            .action(ctx -> {
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length < 1), "CMD_PLAYER_REQ_FROM_CONSOLE");
                ctx.checkPermission(ctx.args.length == 0, "stemcraft.command.stonecutter.other");

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                SMBridge.openStonecutter(targetPlayer, null, true);
            })
            .register();

        return true;
    }
}
