package com.stemcraft.command;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;

public class SMCommand implements CommandExecutor, TabCompleter {
    protected STEMCraft plugin;
    protected HashMap<String, List<String>> commandMap = new HashMap<>();
    protected String[][] tabCompletion = {};

    public List<String> getCommandList() {
        return new ArrayList<>(commandMap.keySet());
    }

    public List<String> getCommandAliases(String command) {
        return commandMap.getOrDefault(command, new ArrayList<>());
    }

    // Example usage
    public void addCommand(String command, String... aliases) {
        commandMap.put(command, Arrays.asList(aliases));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
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
        for (String[] completion : tabCompletion) {
            if(completion.length < argsList.length) {
                continue;
            }

            boolean match = true;
            for (int i = 0; i < argsList.length - 1; i++) {
                if(completion[i].equalsIgnoreCase("%player%")) {
                    if(player(sender.getServer(), argsList[i]).isEmpty()) {
                        match = false;
                        break;
                    }
                } else if (!completion[i].equals(argsList[i])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                if(completion[argsList.length - 1].equalsIgnoreCase("%player%")) {
                    for(String playerName : player(sender.getServer(), argsList[argsList.length - 1])) {
                        tabCompleteList.add(playerName);
                    }
                } else if(completion[argsList.length - 1].startsWith(argsList[argsList.length - 1])) {
                    tabCompleteList.add(completion[argsList.length - 1]);
                }
            }
        }

        return tabCompleteList;
    }

    public static void loadCommands() {
        STEMCraft plugin = STEMCraft.getInstance();
        List<Class<?>> classCommandList = STEMCraft.getClassList("com/stemcraft/command/", false);

        for (Class<?> classCommandItem : classCommandList) {
            if (classCommandItem.getSimpleName().equals("CommandHandler") || classCommandItem.getSimpleName().equals("CommandItem")) {
                continue; // Skip this class
            }

            try {
                Constructor<?> constructor = classCommandItem.getDeclaredConstructor();
                SMCommand commandInstance = (SMCommand) constructor.newInstance();
                List<String> commandList = commandInstance.getCommandList();
                
                for(String command : commandList) {
                    plugin.getCommand(command).setExecutor(commandInstance);

                    List<String> aliases = commandInstance.getCommandAliases(command);
                    if(aliases.size() > 0) {
                        plugin.getCommand(command).setAliases(aliases);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
