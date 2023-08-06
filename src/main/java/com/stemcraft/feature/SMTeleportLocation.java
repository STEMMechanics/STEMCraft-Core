package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import com.stemcraft.SMUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SMTeleportLocation extends SMFeature {
    private List<String> cacheList = new ArrayList<>();

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("TPLOC_USAGE", "Usage: /tploc <name>");
        this.plugin.getLanguageManager().registerPhrase("TPLOC_NOT_EXIST", "&cTeleport location '%NAME%' does not exist");

        this.plugin.getLanguageManager().registerPhrase("TPLOC_SAVE_SUCCESSFUL", "Location saved successfully");
        this.plugin.getLanguageManager().registerPhrase("TPLOC_SAVE_FAILED", "&cFailed to save location");
        this.plugin.getLanguageManager().registerPhrase("TPLOC_DELETE_SUCCESSFUL", "Location deleted successfully");
        this.plugin.getLanguageManager().registerPhrase("TPLOC_DELETE_FAILED", "&cFailed to delete location");


        this.plugin.getDatabaseManager().addMigration("230601229800_CreateTpLocationsTable", (databaseManager) -> {
            databaseManager.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tp_locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "world TEXT," +
                    "x REAL," +
                    "y REAL," +
                    "z REAL," +
                    "yaw REAL," +
                    "pitch REAL)").executeUpdate();
        });

        this.plugin.getCommandManager().registerTabPlaceholder("tplocations", (Server server, String match) -> {
            return this.cacheList;
        });

        String commandName = "teleportlocation";
        String[] aliases = new String[]{"teleportloc", "tploc"};
        String[][] tabCompletions = new String[][]{
            {commandName, "%tplocations%"},
        };

        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
            if(!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYERS_ONLY");
                return true;
            }

            if (!sender.hasPermission("stemcraft.teleport.location")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            if (args.length == 0) {
                this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_USAGE");
                return true;
            }

            try {
                String name = args[0];

                PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
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

                    Location location = new Location(sender.getServer().getWorld(worldName), x, y, z, yaw, pitch);

                    Player player = (Player) sender;
                    this.plugin.delayedTask(1L, (data) -> {
                        player.teleport(location);
                    }, null);
                } else {
                    HashMap<String, String> replacements = new HashMap<>();
                    replacements.put("NAME", name);
                    this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_NOT_EXIST", replacements);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }, aliases, tabCompletions);

        commandName = "listteleportlocation";
        aliases = new String[]{"listteleportloc", "listtploc"};
        tabCompletions = new String[][]{
            {commandName},
        };

        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
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

                PreparedStatement countStatement = this.plugin.getDatabaseManager().prepareStatement("SELECT COUNT(*) FROM tp_locations");
                ResultSet countResultSet = countStatement.executeQuery();

                if (countResultSet.next()) {
                    int rowCount = countResultSet.getInt(1);
                    maxPages = (int) Math.ceil((double) rowCount / 8);
                }

                if(maxPages == 0) {
                    sender.sendMessage(ChatColor.RED + "No teleport locations where found");
                    return true;
                }

                PreparedStatement statement = plugin.getDatabaseManager().prepareStatement(
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
                    DecimalFormat df = new DecimalFormat("#");

                    TextComponent tpName = new TextComponent(ChatColor.GOLD + name + " " + ChatColor.GRAY + df.format(x) + "," + df.format(y) + "," + df.format(z) + " " + worldName);
                    tpName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tploc " + name));
                    tpName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Teleport to X:" + df.format(x) + " Y:" + df.format(y) + " Z:" + df.format(z) + " " + worldName)));

                    TextComponent update = new TextComponent(ChatColor.WHITE + "[Update]");
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

                SMUtil.paginate(sender, title, rows, page, maxPages, baseCommand);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }, aliases, tabCompletions);

        commandName = "addteleportlocation";
        aliases = new String[]{"addteleportloc", "addtploc"};
        tabCompletions = new String[][]{
            {commandName},
        };

        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
            if (!sender.hasPermission("stemcraft.teleport.location.add")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            if (!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                return true;
            }

            if (args.length == 0) {
                this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_USAGE");
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
                PreparedStatement selectStatement = this.plugin.getDatabaseManager().prepareStatement(
                        "SELECT COUNT(*) FROM tp_locations WHERE name = ?");
                selectStatement.setString(1, name);
                ResultSet selectResult = selectStatement.executeQuery();
                selectResult.next();

                int rowCount = selectResult.getInt(1);

                PreparedStatement statement;
                if (rowCount > 0) {
                    // Update existing row
                    statement = this.plugin.getDatabaseManager().prepareStatement(
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
                    statement = this.plugin.getDatabaseManager().prepareStatement(
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
                    this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_SAVE_SUCCESSFUL");
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_SAVE_FAILED");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            this.buildCacheList();
            return true;
        }, aliases, tabCompletions);

        commandName = "delteleportlocation";
        aliases = new String[]{"delteleportloc", "deltploc"};
        tabCompletions = new String[][]{
            {commandName},
        };

        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
            if (!sender.hasPermission("stemcraft.teleport.location.delete")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            String name = args[0];

            if (!this.cacheList.contains(name)) {
                HashMap<String, String> replacements = new HashMap<>();
                replacements.put("NAME", name);
                this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_NOT_EXIST", replacements);
                return true;
            }

            try {
                PreparedStatement statement;
                statement = this.plugin.getDatabaseManager().prepareStatement(
                        "DELETE FROM tp_locations WHERE name = ?");

                statement.setString(1, name);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_DELETE_SUCCESSFUL");
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "TPLOC_DELETE_FAILED");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            this.buildCacheList();
            return true;
        }, aliases, tabCompletions);

        this.buildCacheList();
        return true;
    }

    private void buildCacheList() {
        try {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                "SELECT name FROM tp_locations");
            ResultSet resultSet = statement.executeQuery();

            this.cacheList.clear();
            while(resultSet.next()) {
                String name = resultSet.getString("name");
                this.cacheList.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
