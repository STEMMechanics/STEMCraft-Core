package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SMTeleportAll extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("TPALL_USAGE", "Usage: /tpall <player>|<group>");
        this.plugin.getLanguageManager().registerPhrase("TPALL_PLAYER_OR_GROUP_NOT_FOUND", ":warning_red: &cThat player or group was not found");
        this.plugin.getLanguageManager().registerPhrase("TPALL_PLAYER_NOT_ONLINE", ":warning_red: &cThat player is not online");
        
        this.plugin.getLanguageManager().registerPhrase("TPALL_TO", "Teleported players to %TO_PLAYER_NAME%");
        this.plugin.getLanguageManager().registerPhrase("TPALL_BY", "Teleported to %TO_PLAYER_NAME% by %BY_PLAYER_NAME%");

        String commandName = "tpall";
        String[] aliases = new String[]{};
        String[][] tabCompletions = new String[][]{
            {"tpall", "%player%"},
            {"tpall", "%groups%", "%player%"},
        };

        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
            Player targetPlayer = null;

            if (!sender.hasPermission("stemcraft.teleport.spawn.all") && !sender.hasPermission("stemcraft.teleport.spawn.other")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            if(args.length < 1) {
                if (sender instanceof Player) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "TPALL_USAGE");
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_REQ_FROM_CONSOLE");
                }

                return true;
            } else {
                Location destination = ((Player)sender).getLocation();
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                String toName = ((Player)sender).getName();
                String byName = ((Player)sender).getName();

                targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    SMLuckPerms luckPerms = (SMLuckPerms)this.plugin.getFeatureManager().getFeature("SMLuckPerms");
                    if(luckPerms.groupExists(args[0]) == false) {
                        this.plugin.getLanguageManager().sendPhrase(sender, "TPALL_PLAYER_OR_GROUP_NOT_FOUND");
                        return true;
                    } else {
                        if(args.length > 1) {
                            targetPlayer = Bukkit.getPlayer(args[1]);
                            if (targetPlayer == null) {
                                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                                return true;
                            }

                            destination = targetPlayer.getLocation();
                            toName = targetPlayer.getName();
                        }

                        List<Player> playersWithGroup = players.stream()
                            .filter(player -> luckPerms.playerInGroup(player, args[0]))
                            .collect(Collectors.toList());

                        players = playersWithGroup;
                    }
                } else {
                    if(targetPlayer.isOnline() == false) {
                        this.plugin.getLanguageManager().sendPhrase(sender, "TPALL_PLAYER_NOT_ONLINE");
                        return true;
                    } else {
                        destination = targetPlayer.getLocation();
                        toName = targetPlayer.getName();
                    }
                }

                this.teleportAll((Player)sender, destination, players, toName, byName);
                HashMap<String, String> replacements = new HashMap<>();
                        
                replacements.put("TO_PLAYER_NAME", toName);
                this.plugin.getLanguageManager().sendPhrase(sender, "TPALL_TO", replacements);
            }

            return true;
        }, aliases, tabCompletions);

        return true;
    }

    public void teleportAll(Player sender, Location destination, Collection<? extends Player> players, String toName, String byName) {
        for(Player player: players) {
            player.teleport(destination);

            HashMap<String, String> replacements = new HashMap<>();
                    
            replacements.put("TO_PLAYER_NAME", toName);
            replacements.put("BY_PLAYER_NAME", byName);
            this.plugin.getLanguageManager().sendPhrase(sender, "TPALL_BY", replacements);
        }
    }
}
