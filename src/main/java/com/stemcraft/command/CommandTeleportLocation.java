package com.stemcraft.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.database.SMDatabase;
import com.stemcraft.utility.ChatPaginator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CommandTeleportLocation extends SMCommand {
    ArrayList<String> locations = new ArrayList<String>();

    public CommandTeleportLocation() {
        addCommand("teleportlocation", "teleportloc", "tploc");
        addCommand("listteleportlocation", "listteleportloc", "liststploc");
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
        if (command.getName().equalsIgnoreCase("teleportlocation")) {
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
                statement.setString(1, name);
                ResultSet resultSet = statement.executeQuery();

                if(resultSet.next()) {
                    String worldName = resultSet.getString("world");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    float yaw = resultSet.getFloat("yaw");
                    float pitch = resultSet.getFloat("pitch");

                    // Create a location object from the retrieved data
                    Location location = new Location(sender.getServer().getWorld(worldName), x, y, z, yaw, pitch);

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
        } else if (command.getName().equalsIgnoreCase("listteleportlocation")) {
            Integer page = 1;

            if (args.length > 0) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Usage: /listtploc (page)");
                    return true;
                }
            }

            try {
                Integer maxPages = 1;

                PreparedStatement countStatement = SMDatabase.prepareStatement("SELECT COUNT(*) FROM tp_locations");
                ResultSet countResultSet = countStatement.executeQuery();

                if (countResultSet.next()) {
                    int rowCount = countResultSet.getInt(1);
                    maxPages = (int) Math.ceil((double) rowCount / 8);
                }

                if(maxPages == 0) {
                    sender.sendMessage(ChatColor.RED + "No teleport locations where found");
                    return true;
                }

                PreparedStatement statement = SMDatabase.prepareStatement(
                        "SELECT name, world, x, y, z FROM tp_locations LIMIT ?, 8");
                statement.setInt(1, (page - 1) * 8);
                ResultSet resultSet = statement.executeQuery();

                List<BaseComponent[]> rows = new ArrayList<>();

                while(resultSet.next()) {
                    String name = resultSet.getString("name");
                    String worldName = resultSet.getString("world");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");

                    Player player = (Player)sender;

                    TextComponent tpName = new TextComponent(ChatColor.GOLD + name);
                    DecimalFormat df = new DecimalFormat("#.00");
                    tpName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tploc " + name));
                    tpName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Teleport to X:" + Double.parseDouble(df.format(x)) + " Y:" + Double.parseDouble(df.format(y)) + " Z:" + Double.parseDouble(df.format(z)) + " " + worldName)));

                    TextComponent update = new TextComponent(ChatColor.GRAY + "[Update]");
                    update.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/addtploc " + name));
                    update.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Update location to X:" + Double.parseDouble(df.format(player.getLocation().getX())) + " Y:" + Double.parseDouble(df.format(player.getLocation().getY())) + " Z:" + Double.parseDouble(df.format(player.getLocation().getZ())) + " " + player.getLocation().getWorld().getName())));

                    TextComponent delTp = new TextComponent(ChatColor.RED + "[Del]");
                    delTp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/deltploc " + name));
                    delTp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Delete teleport location")));

                    BaseComponent[] row = new BaseComponent[]{tpName, new TextComponent(" "), update, new TextComponent(" "), delTp};
                    rows.add(row);
                }

                String title = "Teleport Locations";
                String baseCommand = "/listtploc";

                ChatPaginator.display(sender, title, rows, page, maxPages, baseCommand);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        } else if (command.getName().equalsIgnoreCase("addteleportlocation")) {
            if (!sender.hasPermission("stemcraft.tploc.add")) {
                sender.sendMessage("You don't have permission to use this command");
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage("Usage: /addtploc <name>");
                return true;
            }


            Player player = (Player) sender;
            String name = args[0];
            Location location = player.getLocation();

            String world = location.getWorld().getName();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            float yaw = location.getYaw();
            float pitch = location.getPitch();

            try {
                PreparedStatement selectStatement = SMDatabase.prepareStatement(
                        "SELECT COUNT(*) FROM tp_locations WHERE name = ?");
                selectStatement.setString(1, name);
                ResultSet selectResult = selectStatement.executeQuery();
                selectResult.next();

                int rowCount = selectResult.getInt(1);

                PreparedStatement statement;
                if (rowCount > 0) {
                    // Update existing row
                    statement = SMDatabase.prepareStatement(
                            "UPDATE tp_locations SET world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE name = ?");

                    statement.setString(1, world);
                    statement.setDouble(2, x);
                    statement.setDouble(3, y);
                    statement.setDouble(4, z);
                    statement.setFloat(5, yaw);
                    statement.setFloat(6, pitch);
                    statement.setString(7, name);
                } else {
                    // Insert new row
                    statement = SMDatabase.prepareStatement(
                            "INSERT INTO tp_locations (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    statement.setString(1, name);
                    statement.setString(2, world);
                    statement.setDouble(3, x);
                    statement.setDouble(4, y);
                    statement.setDouble(5, z);
                    statement.setFloat(6, yaw);
                    statement.setFloat(7, pitch);
                }

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    sender.sendMessage("Location saved successfully.");
                    // sender.sendMessage("Teleport location '" + name + "' updated.");
                } else {
                    sender.sendMessage("Failed to save location.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return true;
        } else if (command.getName().equalsIgnoreCase("delteleportlocation")) {
            if (!sender.hasPermission("stemcraft.tploc.del")) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            String name = args[0];

            if (!locations.contains(name)) {
                sender.sendMessage("Teleport location '" + name + "' does not exist.");
                return true;
            }

            try {
                PreparedStatement statement;
                statement = SMDatabase.prepareStatement(
                        "DELETE FROM tp_locations WHERE name = ?");

                statement.setString(1, name);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    sender.sendMessage("Location deleted successfully.");
                    // sender.sendMessage("Teleport location '" + name + "' updated.");
                } else {
                    sender.sendMessage("Failed to save location.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }
}
