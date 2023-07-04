package com.stemcraft.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class CommandClearInventory extends SMCommand {

    public CommandClearInventory() {
        addCommand("clearinventory");

        tabCompletion = new String[][]{
            {"clearinventory", "%player%"},
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("clearinventory")) {
            Player player = (Player) sender;
            Player targetPlayer = null;

            if(args.length < 1) {
                if (sender instanceof Player) {
                    targetPlayer = (Player) sender;
                } else {
                    sender.sendMessage(ChatColor.RED + "A player is required to use this command from the console");
                    return true;
                }
            } else {
                if (!sender.hasPermission("stemcraft.inventory.clear.other")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command on other players");
                    return true;
                }

                targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found");
                    return true;
                }
            }

            PlayerInventory inventory = targetPlayer.getInventory();
            inventory.clear();
            inventory.setArmorContents(null);

            player.sendMessage("Inventory cleared for " + targetPlayer.getName());
            return true;
        }
        return false;
    }
}
