package com.stemcraft.feature;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;

/**
 * Teleports the player to the next safest location above them
 */
public class SMTeleportTop extends SMFeature {

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {

        // Command - tptop
        new SMCommand("tptop")
            .alias("teleporttop")
            .alias("top")
            .tabComplete("{player}")
            .permission("stemcraft.command.teleport.top")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");
                ctx.checkPermission(
                    ctx.fromConsole() || targetPlayer.getUniqueId().equals(ctx.player.getUniqueId()),
                    "stemcraft.command.teleport.top.other");

                Location location = targetPlayer.getLocation();
                int y = location.getBlockY();

                // Increment y until a safe spot is found
                while (y < 256) {
                    y++;
                    Location checkLocation =
                        new Location(location.getWorld(), location.getBlockX(), y, location.getBlockZ());
                    if (SMCommon.isSafeLocation(checkLocation)) {
                        SMCommon.delayedPlayerTeleport(targetPlayer,
                            new Location(location.getWorld(), location.getBlockX(), y, location.getBlockZ()));
                        ctx.returnSuccessLocale("TPTOP_TELEPORTED");
                    }
                }

                ctx.returnErrorLocale("TPTOP_NO_SAFE_LOCATION");

            }).register();

        return true;
    }
}
