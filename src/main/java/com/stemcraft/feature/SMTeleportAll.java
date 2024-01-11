package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;

public class SMTeleportAll extends SMFeature {
    @Override
    protected Boolean onEnable() {

        new SMCommand("tpall")
            .tabComplete("g:{group}", "w:{world}", "{player}")
            .action(ctx -> {
                ctx.checkPermission("stemcraft.teleport.spawn.all");

                Collection<? extends Player> teleportPlayers = Bukkit.getOnlinePlayers();

                // check if there is a world argument
                if (ctx.optionArgs.containsKey("w")) {
                    World world = Bukkit.getServer().getWorld(ctx.optionArgs.get("w"));
                    if (world != null) {
                        teleportPlayers = teleportPlayers.stream()
                            .filter(player -> player.getWorld().equals(world))
                            .collect(Collectors.toList());
                    }
                }

                // check if there is a group argument
                if (ctx.optionArgs.containsKey("g")) {
                    if (STEMCraft.featureEnabled("SMLuckPerms")) {
                        if (SMLuckPerms.groupExists(ctx.optionArgs.get("g"))) {
                            teleportPlayers = teleportPlayers.stream()
                                .filter(player -> SMLuckPerms.playerInGroup(player, ctx.optionArgs.get("g")))
                                .collect(Collectors.toList());
                        }
                    }
                }

                Player targetPlayer = ctx.getArgAsPlayer(1, ctx.player);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_REQ_FROM_CONSOLE");

                Location destination = targetPlayer.getLocation();
                String toName = targetPlayer.getName();
                String byName = targetPlayer.getName();
                Collection<Player> selectedPlayers = new ArrayList<>(teleportPlayers);
                selectedPlayers.remove(targetPlayer);

                this.teleportAll(ctx.sender, destination, selectedPlayers, toName,
                    targetPlayer != ctx.player ? byName : null);

                ctx.returnSuccessLocale("TPALL_TO", "player", toName);
            })
            .register();

        return true;
    }

    public void teleportAll(CommandSender sender, Location destination, Collection<? extends Player> players,
        String toName, String byName) {
        for (Player player : players) {
            player.teleport(destination);

            if (byName == null) {
                SMMessenger.infoLocale(player, "TPALL_BY_SAME", "to_player", toName);
            } else {
                SMMessenger.infoLocale(player, "TPALL_BY", "to_player", toName, "by_player", byName);
            }
        }
    }
}
