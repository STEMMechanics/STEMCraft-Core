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

                targetPlayer.openAnvil(null, true);
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

                targetPlayer.openCartographyTable(null, true);
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

                targetPlayer.openGrindstone(null, true);
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

                targetPlayer.openLoom(null, true);
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

                targetPlayer.openSmithingTable(null, true);
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

                targetPlayer.openStonecutter(null, true);
            })
            .register();

        return true;
    }
}
