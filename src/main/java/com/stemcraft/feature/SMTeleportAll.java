package com.stemcraft.feature;

import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;

public class SMTeleportAll extends SMFeature {
    @Override
    protected Boolean onEnable() {

        new SMCommand("tpall")
            .tabComplete("{player}")
            // .tabComplete("{groups}", "{player}")
            .action(ctx -> {
                ctx.checkPermission("stemcraft.teleport.spawn.all");
                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_REQ_FROM_CONSOLE");

                Location destination = targetPlayer.getLocation();
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                String toName = targetPlayer.getName();
                String byName = targetPlayer.getName();

                this.teleportAll(ctx.sender, destination, players, toName, byName);

                ctx.returnSuccessLocale("TPALL_TO", "to", toName);
            })
            .register();

        return true;
    }

    public void teleportAll(CommandSender sender, Location destination, Collection<? extends Player> players, String toName, String byName) {
        for(Player player: players) {
            player.teleport(destination);

            SMMessenger.infoLocale(sender, "TPALL_BY", "to_player", toName, "by_player", byName);
        }
    }
}
