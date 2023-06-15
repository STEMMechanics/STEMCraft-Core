package com.stemcraft.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.database.SMDatabase;

public class CommandTeleportLocation extends SMCommand {
    ArrayList<String> locations = new ArrayList<String>();

    public CommandTeleportLocation() {
        addCommand("teleportlocation", "teleportloc", "tploc");
        addCommand("addteleportlocation", "addteleportloc", "addtploc");
        addCommand("delteleportlocation", "delteleportloc", "deltploc");

        try {
            PreparedStatement statement = SMDatabase.prepareStatement("SELECT name FROM tp_locations WHERE 1");
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                locations.add(resultSet.getString("name"));
            }

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tploc")) {
            try {
                if (!sender.hasPermission("stemcraft.tploc")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command");
                    return true;
                }

                if (args.length == 0) {
                    sender.sendMessage("Usage: /tploc <name>");
                    return true;
                }

                String name = args[0];

                PreparedStatement statement = SMDatabase.prepareStatement(
                    "SELECT world, x, y, z, yaw, pitch FROM tp_locations WHERE name = ?");
                statement.setString(0, name);
                ResultSet resultSet = statement.executeQuery();

                if(resultSet.next()) {
                    String worldName = resultSet.getString("world");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    float yaw = resultSet.getFloat("yaw");
                    float pitch = resultSet.getFloat("pitch");

                    // Create a location object from the retrieved data
                    Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);

                    // Teleport the player to the created location
                    Player player = (Player) sender;
                    player.teleport(location);
                } else {
                    sender.sendMessage(ChatColor.RED + "Teleport location '" + name + "' does not exist");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        // } else if (command.getName().equalsIgnoreCase("addtploc")) {
        //     if (!sender.hasPermission("stemcraft.tploc.add")) {
        //         sender.sendMessage("You don't have permission to use this command.");
        //         return true;
        //     }

        //     if (!(sender instanceof Player)) {
        //         sender.sendMessage("This command can only be used by players.");
        //         return true;
        //     }

        //     Player player = (Player) sender;
        //     String name = args[0];
        //     Location location = player.getLocation();

        //     if (locations.contains(name)) {
        //         updateLocation(name, location);
        //         sender.sendMessage("Teleport location '" + name + "' updated.");
        //     } else {
        //         addLocation(name, location);
        //         sender.sendMessage("Teleport location '" + name + "' added.");
        //     }

        //     return true;
        // } else if (command.getName().equalsIgnoreCase("deltploc")) {
        //     if (!hasPermission(sender, "tpplugin.deltploc")) {
        //         sender.sendMessage("You don't have permission to use this command.");
        //         return true;
        //     }

        //     String name = args[0];

        //     if (!locations.contains(name)) {
        //         sender.sendMessage("Teleport location '" + name + "' does not exist.");
        //         return true;
        //     }

        //     deleteLocation(name);
        //     sender.sendMessage("Teleport location '" + name + "' deleted.");

        //     return true;
        }

        return false;
    }
}
