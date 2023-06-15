package com.stemcraft.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGameMode extends SMCommand {

    public CommandGameMode() {
        addCommand("gma");
        addCommand("gmc");
        addCommand("gms");
        addCommand("gmsp");

        tabCompletion = new String[][]{
            {"gma", "%player%"},
            {"gmc", "%player%"},
            {"gms", "%player%"},
            {"gmsp", "%player%"},
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Player targetPlayer = null;
        String gamemodeStr = "Unknown";

        if(args.length < 1) {
            if (sender instanceof Player) {
                targetPlayer = (Player) sender;
            } else {
                sender.sendMessage(ChatColor.RED + "A player is required to use this command from the console");
                return true;
            }
        } else {
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player not found");
                return true;
            }
        }
    
        if (command.getName().equalsIgnoreCase("gma")) {
            targetPlayer.setGameMode(GameMode.ADVENTURE);
            gamemodeStr = "Adventure";
        } else if (command.getName().equalsIgnoreCase("gmc")) {
            targetPlayer.setGameMode(GameMode.CREATIVE);
            gamemodeStr = "Creative";
        } else if (command.getName().equalsIgnoreCase("gms")) {
            targetPlayer.setGameMode(GameMode.SURVIVAL);
            gamemodeStr = "Survival";
        } else if (command.getName().equalsIgnoreCase("gmsp")) {
            targetPlayer.setGameMode(GameMode.SPECTATOR);
            gamemodeStr = "Spectator";
        } else {
            player.sendMessage(ChatColor.RED + "Unknown Gamemode");
            return true;
        }
            
        player.sendMessage("Gamemode for " + targetPlayer.getName() + " set to " + gamemodeStr);
        return true;
    }
}
