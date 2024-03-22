package com.stemcraft.feature;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;

public class SMGameMode extends SMFeature {
    @Override
    protected Boolean onEnable() {
        new SMCommand("gm")
            .alias("gma", "gmc", "gms", "gmsp")
            .tabComplete("{player}")
            .permission("minecraft.command.gamemode")
            .action(ctx -> {
                ctx.checkNotConsole();

                Player targetPlayer = ctx.player;
                String gamemodeStr = "Unknown";

                if (!ctx.args.isEmpty()) {
                    targetPlayer = SMCommon.findPlayer(ctx.args.get(0));
                    if (targetPlayer == null) {
                        ctx.returnErrorLocale("CMD_PLAYER_NOT_FOUND");
                        return;
                    }
                }

                if ("gma".equals(ctx.alias)) {
                    targetPlayer.setGameMode(GameMode.ADVENTURE);
                    gamemodeStr = "Adventure";
                } else if ("gmc".equals(ctx.alias)) {
                    targetPlayer.setGameMode(GameMode.CREATIVE);
                    gamemodeStr = "Creative";
                } else if ("gms".equals(ctx.alias)) {
                    targetPlayer.setGameMode(GameMode.SURVIVAL);
                    gamemodeStr = "Survival";
                } else if ("gmsp".equals(ctx.alias)) {
                    targetPlayer.setGameMode(GameMode.SPECTATOR);
                    gamemodeStr = "Spectator";
                } else {
                    ctx.returnErrorLocale("GAMEMODE_UNKNOWN");
                }

                if (targetPlayer == ctx.sender) {
                    SMMessenger.infoLocale(targetPlayer, "GAMEMODE_CHANGED", "gamemode", gamemodeStr);
                } else {
                    SMMessenger.infoLocale(ctx.sender, "GAMEMODE_CHANGED_FOR", "player", targetPlayer.getName(),
                        "gamemode", gamemodeStr);
                    SMMessenger.infoLocale(targetPlayer, "GAMEMODE_CHANGED_BY", "player", ctx.senderName(), "gamemode",
                        gamemodeStr);
                }

            })
            .register();


        // String[][] tabCompletions = new String[][]{
        // {"gma", "%player%"},
        // {"gmc", "%player%"},
        // {"gms", "%player%"},
        // {"gmsp", "%player%"},
        // };

        return true;
    }
}
