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
 * Teleports the player to the next safest location above them
 */
public class SMTeleportTop extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {

        new SMCommand("tptop")
            .alias("top")
            .tabComplete("{player}")
            .permission("stemcraft.command.teleport.top")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                ctx.checkPermission(ctx.isConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()), "stemcraft.command.teleport.top.other");

                Location location = targetPlayer.getLocation();
                int y = location.getBlockY();

                // Increment y until a safe spot is found
                while (y < 256) {
                    y++;
                    Location checkLocation = new Location(location.getWorld(), location.getBlockX(), y, location.getBlockZ());
                    if (isSafeLocation(checkLocation)) {
                        SMCommon.delayedPlayerTeleport(targetPlayer, new Location(location.getWorld(), location.getBlockX(), y, location.getBlockZ()));
                        ctx.returnSuccessLocale("TPTOP_TELEPORTED")
                    }
                }

                ctx.returnErrorLocale("TPTOP_TELEPORTED");
                
            })
            .register();

        return true;
    }
}
