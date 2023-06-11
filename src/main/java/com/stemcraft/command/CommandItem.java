package com.stemcraft.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;

public class CommandItem implements CommandExecutor, TabCompleter {
    protected STEMCraft plugin;
    protected ArrayList<String> commandList = new ArrayList<>();
    protected String[][] tabCompletion = {};

    public ArrayList<String> commandList() {
        return commandList;
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
