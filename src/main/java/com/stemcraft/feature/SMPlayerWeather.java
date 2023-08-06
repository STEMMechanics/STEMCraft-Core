package com.stemcraft.feature;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

public class SMPlayerWeather extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_USAGE", "Usage: /pweather <type> (player)");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_LOOKUP_SERVER", "Weather reset to server conditions");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_LOOKUP_FOR", "Weather for %PLAYER_NAME% reset to server conditions");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_RESET", "Weather reset to server conditions");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_RESET_FOR", "Weather for %PLAYER_NAME% reset to server conditions");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_RESET_BY", "Weather reset to server conditions by %PLAYER_NAME%");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_GET", "Weather is set to %TYPE%");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_GET_FOR", "Weather for %PLAYER_NAME% is set to %TYPE%");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_SET", "Weather set to %TYPE%");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_SET_FOR", "Weather for %PLAYER_NAME% set to %TYPE%");
        this.plugin.getLanguageManager().registerPhrase("PWEATHER_SET_BY", "Weather set to %TYPE% by %PLAYER_NAME%");

        String[] aliases = new String[]{"pweather"};
        String[][] tabCompletions = new String[][]{
            {"playerweather", "reset", "%player%"},
            {"playerweather", "clear", "%player%"},
            {"playerweather", "rain", "%player%"},
            {"playerweather", "lookup", "%player%"},
        };

        this.plugin.getCommandManager().registerCommand("playerweather", (sender, command, label, args) -> {
            Player targetPlayer = null;
            String weatherOption = "lookup";

            if(args.length < 1) {
                if (sender instanceof Player) {
                    targetPlayer = (Player) sender;
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_USAGE");
                    return true;
                }
            } else {
                weatherOption = args[0];

                if(args.length < 2) {
                    if (sender instanceof Player) {
                        targetPlayer = (Player) sender;
                    } else {
                        this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_REQ_FROM_CONSOLE");
                        return true;
                    }
                } else {
                    targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer == null) {
                        this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                        return true;
                    }
                }
            }

            if(sender instanceof Player) {
                if(!sender.hasPermission("stemcraft.pweather")) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                    return true;
                }

                if(!sender.hasPermission("stemcraft.pweather.other") && targetPlayer != sender) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                    return true;
                }
            }

            HashMap<String, String> weatherTypes = new HashMap<>();
            weatherTypes.put("lookup", "");
            weatherTypes.put("reset", "");
            weatherTypes.put("clear", "CLEAR");
            weatherTypes.put("rain", "DOWNFALL");

            if(!weatherTypes.containsKey(weatherOption)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_INVALID_OPTION");
                return true;
            }

            if(weatherOption.equalsIgnoreCase("lookup")) {
                String weatherValue = "server";

                WeatherType playerWeather = targetPlayer.getPlayerWeather();
                if(playerWeather != null) {
                    if(playerWeather == WeatherType.CLEAR) {
                        weatherValue = "clear";
                    } else if(playerWeather == WeatherType.DOWNFALL) {
                        weatherValue = "rain";
                    } else {
                        weatherValue = "unknown";
                    }
                }

                HashMap<String, String> replacements = new HashMap<>();
                replacements.put("TYPE", weatherValue);
                
                if(targetPlayer == sender) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_GET", replacements);
                } else {
                    replacements.put("PLAYER_NAME", targetPlayer.getName());
                    this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_GET_FOR", replacements);
                }
            } else if(weatherOption.equalsIgnoreCase("reset")) {
                targetPlayer.resetPlayerWeather();

                if(targetPlayer == sender) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_RESET");
                } else {
                    HashMap<String, String> replacements = new HashMap<>();
                            
                    replacements.put("PLAYER_NAME", targetPlayer.getName());
                    this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_RESET_FOR", replacements);

                    replacements.put("PLAYER_NAME", sender.getName());
                    this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_RESET_BY", replacements);
                }
            } else {
                String weatherValue = weatherTypes.getOrDefault(weatherOption, "");
                if(weatherValue.length() > 0) {
                    try {
                        WeatherType weatherType = WeatherType.valueOf(weatherValue);

                        targetPlayer.setPlayerWeather(weatherType);

                        HashMap<String, String> replacements = new HashMap<>();
                        replacements.put("TYPE", weatherOption.toLowerCase());
                        
                        if(targetPlayer == sender) {
                            this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_SET", replacements);
                        } else {
                            replacements.put("PLAYER_NAME", targetPlayer.getName());
                            this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_SET_FOR", replacements);

                            replacements.put("PLAYER_NAME", sender.getName());
                            this.plugin.getLanguageManager().sendPhrase(sender, "PWEATHER_SET_BY", replacements);
                        }
                    } catch (IllegalArgumentException e) {
                        /* Do nothing */
                    }
                }
            }

            return true;
        }, aliases, tabCompletions);

        return true;
    }
}
