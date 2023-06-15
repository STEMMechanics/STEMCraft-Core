package com.stemcraft.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.WeatherType;

public class CommandPlayerWeather extends SMCommand {

    public CommandPlayerWeather() {
        addCommand("playerweather", "pweather");

        tabCompletion = new String[][]{
            {"pweather", "reset", "%player%"},
            {"pweather", "clear", "%player%"},
            {"pweather", "rain", "%player%"},
            {"pweather", "thunder", "%player%"},
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pweather")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                return true;
            }

            String[][] matrix = new String[][] {
                {"reset", "", "Reset the weather"},
                {"clear", "CLEAR", "Set the weather to clear"},
                {"rain", "DOWNFALL", "Set the weather to rain"},
                {"thunder", "THUNDERSTORM", "Set the weather to rain & thunder"},
            };

            Player player = (Player) sender;
            Player targetPlayer = player;

            if (!player.hasPermission("stemcraft.pweather") && !player.hasPermission("stemcraft.pweather.other")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command");
                return true;
            }

            if (args.length < 1) {
                player.sendMessage("Usage: /pweather <type> (player)");
                return true;
            } if(args.length > 1) {
                if (!player.hasPermission("stemcraft.pweather.other")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to change other players' weather");
                    return true;
                }

                String playerName = args[1];

                targetPlayer = Bukkit.getPlayer(playerName);
                if (targetPlayer == null) {
                    player.sendMessage(ChatColor.RED + "Player not found");
                    return true;
                }
            }

            boolean match = false;
            String weatherTypeString = "";
            String result = "";

            for (String[] item : matrix) {
                if (args[0].equalsIgnoreCase(item[0])) {
                    match = true;
                    weatherTypeString = item[1];
                    result = item[2];
                    break;
                }
            }

            if(match) {
                WeatherType weatherType = null;
                
                try {
                    if(!weatherTypeString.equals("")) {
                        weatherType = WeatherType.valueOf(weatherTypeString);
                    }

                    targetPlayer.setPlayerWeather(weatherType);
                    player.sendMessage(result + " for player" + (args.length > 1 ? " " + targetPlayer.getName() : ""));
                    return true;
                } catch (IllegalArgumentException e) {
                    // Do nothing
                }
            }

            player.sendMessage(ChatColor.RED + "Invalid weather type");
            return true;
        }
        return false;
    }
}
