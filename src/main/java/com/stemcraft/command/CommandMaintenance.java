package com.stemcraft.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import com.stemcraft.utility.Meta;

public class CommandMaintenance extends SMCommand {

    public CommandMaintenance() {
        addCommand("maintenance");

        tabCompletion = new String[][]{
            {"maintenance", "on"},
            {"maintenance", "off"},
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("maintenance")) {
            if(args.length > 0) {
                if(!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
                    String usage = "";
                    PluginCommand pluginCommand = plugin.getCommand(command.getName());

                    if (pluginCommand != null) {
                        usage = pluginCommand.getUsage();
                    }

                    sender.sendMessage(ChatColor.RED + "Usage " + usage);
                    return true;
                }

                if(args[0].equalsIgnoreCase("on")) {
                    Meta.setBoolean("maintenance", true);
                    sender.sendMessage(ChatColor.YELLOW + "Maintenance is now enabled");
                } else {
                    Meta.setBoolean("maintenance", false);
                    sender.sendMessage(ChatColor.YELLOW + "Maintenance is now disabled");
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Maintenance is " + (Meta.getBoolean("maintenance", false) ? "enabled" : "disabled"));
            }

            return true;
        }

        return false;
    }
}
