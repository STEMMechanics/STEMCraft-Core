package com.stemcraft.feature;

import org.bukkit.entity.Player;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;

public class SMTeleportSpawn extends SMFeature {
    
    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        new SMCommand("tpspawn")
            .alias("teleportspawn")
            .permission("stemcraft.teleport.spawn")
            .tabComplete("{player}")
            .action(ctx -> {
                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);

                // Check player exists when issued from console
                ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.length == 0), "CMD_PLAYER_REQ_FROM_CONSOLE");

                // Check player has permission to teleport others
                ctx.checkBooleanLocale(targetPlayer == ctx.sender || ctx.hasPermission("stemcraft.teleport.spawn.other"), "CMD_NO_PERMISSION");

                // Check target player exists
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                // Teleport player
                targetPlayer.teleport(targetPlayer.getWorld().getSpawnLocation());

                // Notify players
                if(targetPlayer == ctx.sender) {
                    ctx.returnInfoLocale("TPSPAWN");
                } else {
                    SMMessenger.infoLocale(ctx.sender, "TPSPAWN_FOR", "player", targetPlayer.getName());
                    SMMessenger.infoLocale(targetPlayer, "TPSPAWN_BY", "player", ctx.senderName());
                }
            })
            .register();

        return true;
    }
}
