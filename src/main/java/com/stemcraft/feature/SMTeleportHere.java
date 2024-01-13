package com.stemcraft.feature;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;

public class SMTeleportHere extends SMFeature {

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {

        // Command - tphere
        new SMCommand("tphere")
            .tabComplete("{player}")
            .permission("stemcraft.command.teleport.here")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotConsole();
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                SMCommon.safePlayerTeleport(targetPlayer, ctx.player.getLocation());
                SMMessenger.infoLocale(targetPlayer, "TPHERE_TELEPORTED", "player", ctx.player.getName());
            }).register();

        return true;
    }
}
