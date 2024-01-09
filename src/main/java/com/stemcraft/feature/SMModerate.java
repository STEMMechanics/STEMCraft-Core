package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;

/**
 * Provides player moderation tools
 */
public class SMModerate extends SMFeature {

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {

        /** Register TabCompletion Ban Duration */
        SMTabComplete.register("ban-duration", () -> {
            String[] completions = {"10m", "1h", "1d", "28d", "perm"};
            return Arrays.asList(completions);
        });


        /** Ban command */
        new SMCommand("ban")
            .tabComplete("{player}", "{ban-duration}")
            .permission("stemcraft.command.ban")
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

                ctx.returnErrorLocale("TPTOP_TELEPORTED");

            }).register();

        return true;
    }
}
