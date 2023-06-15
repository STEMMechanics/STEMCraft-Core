package com.stemcraft.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTeleportSpawn extends SMCommand {
    public CommandTeleportSpawn() {
        addCommand("teleportspawn", "tpspawn");

        tabCompletion = new String[][]{
            {"%player%"},
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tpspawn")) {
            Player targetPlayer = null;

            if (!sender.hasPermission("stemcraft.tp.spawn") && !sender.hasPermission("stemcraft.tp.spawn.other")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command");
                return true;
            }

            if(args.length < 1) {
                if (sender instanceof Player) {
                    targetPlayer = (Player) sender;
                } else {
                    sender.sendMessage(ChatColor.RED + "A player is required to use this command from the console");
                    return true;
                }
            } else {
                if (!sender.hasPermission("stemcraft.tp.spawn.other")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command on other players");
                    return true;
                }

                targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found");
                    return true;
                }
            }
            
            targetPlayer.teleport(targetPlayer.getWorld().getSpawnLocation());
            sender.sendMessage("Teleported " + targetPlayer.getName() + " to the world spawn point");
            return true;
        }
        
        return false;
    }
}
