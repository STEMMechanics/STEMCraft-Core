package com.stemcraft.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.utility.Util;

public class CommandNickName extends SMCommand {

    public CommandNickName() {
        addCommand("nickname", "nick");

        tabCompletion = new String[][]{
            {"nickname", "%player%"},
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nickname")) {
            Player player = (Player) sender;
            Player targetPlayer = player;
            String nickname = "off";

            if(args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "A player is required to use this command from the console");
                    return true;
                }

                nickname = args[0];
            } else if(args.length > 1) {
                if (!sender.hasPermission("stemcraft.nickname.other")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command on other players");
                    return true;
                }

                targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    targetPlayer = Util.getPlayerByDisplayName(args[0]);
                    if (targetPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found");
                        return true;
                    }
                }

                nickname = args[1];
            } else {
                nickname = "off";
            }

            if(!nickname.equalsIgnoreCase("off")) {
                Player foundPlayer = Bukkit.getPlayer(nickname);
                if(foundPlayer == null) {
                    foundPlayer = Util.getPlayerByDisplayName(nickname);
                }

                if(foundPlayer != null && foundPlayer != targetPlayer) {
                    sender.sendMessage(ChatColor.RED + "There is already a player named " + nickname);
                    return true;
                }

                sender.sendMessage("The player " + targetPlayer.getDisplayName() + " is now known as " + nickname);
            } else {
                targetPlayer.setDisplayName(targetPlayer.getName());
                sender.sendMessage("The player " + targetPlayer.getDisplayName() + " is now known as " + nickname);
            }

            return true;
        }
        return false;
    }
}
