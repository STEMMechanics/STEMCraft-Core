package com.stemcraft.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import com.stemcraft.component.ComponentLockdown;
import com.stemcraft.utility.Meta;

public class CommandLockdown extends SMCommand {

    public CommandLockdown() {
        addCommand("lockdown");

        tabCompletion = new String[][]{
            {"lockdown", "off"},
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lockdown")) {
            if(args.length > 0) {
                if(args[0].equalsIgnoreCase("off")) {
                    Meta.clear("lockdown");
                    ComponentLockdown.blockedPlayers.clear();

                    sender.sendMessage(ChatColor.YELLOW + "Lockdown has been turned off");
                    return true;
                } else {
                    String code = args[0].toLowerCase();

                    sender.sendMessage(ChatColor.YELLOW + "Lockdown code has been set to " + ChatColor.RESET + code);
                    Meta.setString("lockdown", code);
                }
            } else {
                String code = Meta.getString("lockdown", "");
                
                if(code.length() > 0) {
                    sender.sendMessage(ChatColor.YELLOW + "Lockdown code is set to " + ChatColor.RESET + code);
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Lockdown is turned off");
                }
            }

            return true;
        }

        return false;
    }
}
