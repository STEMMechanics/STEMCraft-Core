package com.stemcraft.command;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.component.ComponentTeleportBack;

public class CommandBack extends SMCommand {

    public CommandBack() {
        addCommand("back");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("back")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID playerId = player.getUniqueId();
                
                if (!ComponentTeleportBack.playerPreviousLocations.containsKey(playerId)) {
                    player.sendMessage("There is no location to teleport back to");
                    return true;
                }

                Location previousLocation = ComponentTeleportBack.playerPreviousLocations.get(playerId);
                player.teleport(previousLocation);
                return true;
            }

            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        return false;
    }
}
