package com.stemcraft.feature;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMeta;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.event.SMEvent;

public class SMMaintenance extends SMFeature {
    private String permission = "stemcraft.maintenance";

    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerLoginEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();

            if (SMMeta.getBool("maintenance", false) && !player.hasPermission(permission)) {
                // Maintenance mode is enabled and player does not have the permission
                ctx.event.setKickMessage(SMLocale.get(player, "MAINTENANCE_KICK"));
                ctx.event.setResult(Result.KICK_OTHER);
            }
        });

        String[] modes = {"enable", "disable"};
        
        new SMCommand("maintenance")
            .permission(permission)
            .tabComplete(modes)
            .action(ctx -> {
                if(ctx.args.length > 0) {
                    String mode = ctx.args[0].toLowerCase();
                    ctx.checkInArrayLocale(modes, mode, "MAINTENANCE_USAGE");

                    if("enable".equals(mode)) {
                        SMMeta.set("maintenance", true);

                        STEMCraft.getPlugin().getServer().getOnlinePlayers().forEach(player -> {
                            if (!player.hasPermission(this.permission)) {
                                player.kickPlayer(SMLocale.get(player, "MAINTENANCE_KICK"));
                            }
                        });
                    } else {
                        SMMeta.set("maintenance", false);
                    }
                } else {
                    String modeString = SMMeta.getBool("maintenance", false) ? "enabled" : "disabled";
                    ctx.returnInfoLocale("MAINTENANCE_SET_TO", "mode", modeString);
                }
            })
            .register();

        return true;
    }
}
