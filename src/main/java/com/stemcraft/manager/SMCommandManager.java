package com.stemcraft.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SMCommandManager extends SMManager implements TabCompleter {
    protected HashMap<String, BiFunction<Server, String, List<String>>> tabCompletionPlaceholders = new HashMap<>();
    protected HashMap<String, BiFunction<CommandSender, String[], Boolean>> stemcraftOptions = new HashMap<>();
    protected String[][] tabCompletionList = {};

    @Override
    public void onEnable() {
        this.registerTabPlaceholder("player", (Server server, String match) -> {
            List<String> playerList = new ArrayList<>();

            for(Player player : server.getOnlinePlayers()) {
                String playerName = player.getName();
                if(playerName.startsWith(match)) {
                    playerList.add(playerName);
                }
            }

            return playerList;
        });

        this.plugin.getCommandManager().registerCommand("stemcraft", (sender, command, label, args) -> {
            Boolean result = true;

            if(args.length < 1) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_OPTION_REQUIRED");
            } else {
                if(this.stemcraftOptions.containsKey(args[0])) {
                    BiFunction<CommandSender, String[], Boolean> biFunction = this.stemcraftOptions.get(args[0]);

                    result = biFunction.apply(sender, args);
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_INVALID_OPTION");
                }
            }

            return result;
        });
    }

    public CommandMap getCommandMap() {
        try {
            final Field bukkitCommandMap = this.plugin.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(this.plugin.getServer());
            return commandMap;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void registerCommand(String name, CommandExecutor executor) {
        this.registerCommand(name, executor, (String[])null, (String[][])null);
    }

    public void registerCommand(String name, CommandExecutor executor, String[] aliases) {
        this.registerCommand(name, executor, aliases, (String[][])null);
    }

    public void registerCommand(String name, CommandExecutor executor, String[] aliases, String[][] tabCompletions) {
        CommandMap commandMap = this.getCommandMap();

        if(commandMap != null) {
            PluginCommand cmd = null;

            try {
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);

                cmd = c.newInstance(name, this.plugin);
            } catch(Exception e) {
                e.printStackTrace();
            }
        
            if(cmd != null) {
                this.addTabCompletions(tabCompletions);

                cmd.setExecutor(executor);
                cmd.setTabCompleter(this);

                if (aliases != null && aliases.length > 0) {
                    cmd.setAliases(Arrays.asList(aliases));
                }

                this.getCommandMap().register(name, "stemcraft", cmd);
            }
        }
    }

    public void addTabCompletions(String[][] tabCompletions) {
        if(tabCompletions != null && tabCompletions.length > 0) {
            int newSize = this.tabCompletionList.length + tabCompletions.length;
            String[][] combinedArray = new String[newSize][];

            System.arraycopy(this.tabCompletionList, 0, combinedArray, 0, this.tabCompletionList.length);
            System.arraycopy(tabCompletions, 0, combinedArray, this.tabCompletionList.length, tabCompletions.length);
            this.tabCompletionList = combinedArray;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tabCompleteList = new ArrayList<>();

        String[] argsList = new String[args.length + 1];
        argsList[0] = cmd.getName();
        System.arraycopy(args, 0, argsList, 1, args.length);

        if (!(sender instanceof Player) || args.length == 0) {
            return null;
        }

        // Check if the current args match the elements in tabCompletion
        for (String[] completion : this.tabCompletionList) {
            if(completion.length < argsList.length) {
                continue;
            }

            boolean match = true;
            for (int i = 0; i < argsList.length - 1; i++) {
                if(completion[i].startsWith("%") && completion[i].endsWith("%")) {
                    String key = completion[i].substring(1, completion[i].length() - 1);
                    
                    if (this.tabCompletionPlaceholders.containsKey(key)) {
                        BiFunction<Server, String, List<String>> biFunction = tabCompletionPlaceholders.get(key);

                        if(biFunction.apply(sender.getServer(), argsList[i]).isEmpty()) {
                            match = false;
                            break;
                        }
                    }
                } else if (!completion[i].equals(argsList[i])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                String targetCompletion = completion[argsList.length - 1];

                if(targetCompletion.startsWith("%") && targetCompletion.endsWith("%")) {
                    String key = targetCompletion.substring(1, targetCompletion.length() - 1);
                    
                    if (this.tabCompletionPlaceholders.containsKey(key)) {
                        BiFunction<Server, String, List<String>> biFunction = tabCompletionPlaceholders.get(key);

                        for(String valueItem : biFunction.apply(sender.getServer(), argsList[argsList.length - 1])) {
                            tabCompleteList.add(valueItem);
                        }
                    }
                } else if(targetCompletion.startsWith(argsList[argsList.length - 1])) {
                    tabCompleteList.add(targetCompletion);
                }
            }
        }

        return tabCompleteList;
    }

    public void registerTabPlaceholder(String placeholder, BiFunction<Server, String, List<String>> callback) {
        this.tabCompletionPlaceholders.put(placeholder, callback);
    }

    public void registerStemCraftOption(String option, BiFunction<CommandSender, String[], Boolean> callback) {
        this.registerStemCraftOption(option, callback, null);
    }
    
    public void registerStemCraftOption(String option, BiFunction<CommandSender, String[], Boolean> callback, String[][] tabCompletions) {
        this.stemcraftOptions.put(option, callback);
        if(tabCompletions != null) {
            this.addTabCompletions(tabCompletions);
        } else {
            this.addTabCompletions(new String[][] {{"stemcraft", option}});
        }
    }

    protected List<String> player(Server server, String match) {
        List<String> playerList = new ArrayList<>();

        for(Player player : server.getOnlinePlayers()) {
            String playerName = player.getName();
            if(playerName.startsWith(match)) {
                playerList.add(playerName);
            }
        }

        return playerList;
    }
}
