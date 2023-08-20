package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class SMSpeed extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("TPALL_USAGE", "Usage: /tpall <player>|<group>");
        this.plugin.getLanguageManager().registerPhrase("TPALL_PLAYER_OR_GROUP_NOT_FOUND", ":warning_red: &cThat player or group was not found");
        this.plugin.getLanguageManager().registerPhrase("TPALL_PLAYER_NOT_ONLINE", ":warning_red: &cThat player is not online");
        
        this.plugin.getLanguageManager().registerPhrase("TPALL_TO", "Teleported players to %TO_PLAYER_NAME%");
        this.plugin.getLanguageManager().registerPhrase("TPALL_BY", "Teleported to %TO_PLAYER_NAME% by %BY_PLAYER_NAME%");

        // Tab Completion - Type
        this.plugin.getCommandManager().registerTabPlaceholder("speedtype", (Server server, String match) -> {
            return this.tabCompletionTypes();
        });

        // Tab Completion - Speed
        this.plugin.getCommandManager().registerTabPlaceholder("speed", (Server server, String match) -> {
            List<String> speed = new ArrayList<>();

            speed.add("1");
            speed.add("1.5");
            speed.add("1.75");
            speed.add("2");

            return speed;
        });

        String commandName = "speed";
        String[] aliases = new String[]{};
        String[][] tabCompletions = new String[][]{
            {"speed", "%speedtype%", "%speed%", "%player%"},
            {"speed", "%speed%", "%player%"},
        };

        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
            Integer optionsIndex = 0;
            String[] options = args;
            
            String type = "";
            float speed = -1;
            Player targetPlayer = null;

            if(sender instanceof Player) {
                targetPlayer = (Player)sender;
            }

            if(args.length > 0) {
                String speedStr = "";

                if(this.tabCompletionTypes().contains(args[0])) {
                    type = args[0];
                    if(args.length > 1) {
                        speedStr = args[1];
                        if(args.length > 2) {
                            targetPlayer = Bukkit.getPlayer(args[2]);
                        }
                    }
                } else {
                    speedStr = args[0];
                    if(args.length > 1) {
                        targetPlayer = Bukkit.getPlayer(args[1]);
                    }
                }

                if(speedStr != "" && targetPlayer != null) {
                    try {
                        speed = Float.parseFloat(speedStr);
                    } catch(NumberFormatException e) {
                        /* empty */
                    }
                }
            }

            if(args.length > 3 || speed == -1) {
                this.plugin.getLanguageManager().sendPhrase(sender, "SPEED_USAGE");
                return true;
            }

            if (!sender.hasPermission("stemcraft.speed")) {
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

                // targetPlayer.isF

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

    private List<String> tabCompletionTypes() {
        List<String> types = new ArrayList<>();

        types.add("walk");
        types.add("fly");

        return types;
    }
}
