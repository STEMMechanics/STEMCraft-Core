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
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMPaginate;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SMTeleportLocation extends SMFeature {
    private HashMap<String, Location> cacheList = new HashMap<>();

    @Override
    protected Boolean onEnable() {
        SMDatabase.runMigration("230601229800_CreateTpLocationsTable", () -> {
            SMDatabase.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tp_locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "world TEXT," +
                    "x REAL," +
                    "y REAL," +
                    "z REAL," +
                    "yaw REAL," +
                    "pitch REAL)")
                .executeUpdate();
        });

        SMTabComplete.register("tplocations", () -> {
            return List.copyOf(this.cacheList.keySet());
        });

        new SMCommand("teleportlocation")
            .alias("teleportloc", "tploc")
            .tabComplete("{tplocations}")
            .permission("stemcraft.teleport.location")
            .action(ctx -> {
                ctx.checkNotConsole();
                ctx.checkArgsLocale(1, "TPLOC_USAGE");
                String name = ctx.args.get(0).toLowerCase();

                if (!cacheList.containsKey(name)) {
                    ctx.returnErrorLocale("TPLOC_NOT_EXIST", "name", ctx.args.get(0));
                }

                SMCommon.delayedPlayerTeleport(ctx.player, cacheList.get(name));
            })
            .register();

        new SMCommand("listteleportlocation")
            .alias("listteleportloc", "listtploc")
            .action(ctx -> {
                Integer itemCount = 0;

                try {
                    PreparedStatement countStatement = SMDatabase.prepareStatement("SELECT COUNT(*) FROM tp_locations");
                    ResultSet countResultSet = countStatement.executeQuery();

                    if (countResultSet.next()) {
                        itemCount = countResultSet.getInt(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new SMPaginate(ctx.sender, ctx.getArgInt(1, 1))
                    .count(itemCount)
                    .command("listtploc")
                    .title("Teleport Locations")
                    .none("No teleport locations where found")
                    .showItems((start, max) -> {
                        List<BaseComponent[]> rows = new ArrayList<>();

                        try {
                            PreparedStatement statement = SMDatabase.prepareStatement(
                                "SELECT name, world, x, y, z FROM tp_locations LIMIT ?, ?");
                            statement.setInt(1, start);
                            statement.setInt(2, max);
                            ResultSet resultSet = statement.executeQuery();

                            while (resultSet.next()) {
                                String name = resultSet.getString("name");
                                String worldName = resultSet.getString("world");
                                double x = resultSet.getDouble("x");
                                double y = resultSet.getDouble("y");
                                double z = resultSet.getDouble("z");

                                DecimalFormat df = new DecimalFormat("#");

                                TextComponent tpName = new TextComponent(ChatColor.GOLD + name + " " + ChatColor.GRAY
                                    + df.format(x) + "," + df.format(y) + "," + df.format(z) + " " + worldName);
                                tpName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tploc " + name));
                                tpName.setHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Teleport to X:" + df.format(x)
                                        + " Y:" + df.format(y) + " Z:" + df.format(z) + " " + worldName)));

                                TextComponent update = new TextComponent(ChatColor.WHITE + "[Update]");
                                update
                                    .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/addtploc " + name));
                                update.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new Text("Update location to X:"
                                        + Double.parseDouble(df.format(ctx.player.getLocation().getX())) + " Y:"
                                        + Double.parseDouble(df.format(ctx.player.getLocation().getY())) + " Z:"
                                        + Double.parseDouble(df.format(ctx.player.getLocation().getZ())) + " "
                                        + ctx.player.getLocation().getWorld().getName())));

                                TextComponent delTp = new TextComponent(ChatColor.RED + "[Del]");
                                delTp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/deltploc " + name));
                                delTp.setHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Delete teleport location")));

                                BaseComponent[] row = new BaseComponent[] {tpName, new TextComponent(" "), update,
                                        new TextComponent(" "), delTp};
                                rows.add(row);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return rows;
                    });
            })
            .register();

        new SMCommand("addteleportlocation")
            .alias("addteleportloc", "addtploc")
            .permission("stemcraft.teleport.location.add")
            .action(ctx -> {
                ctx.checkNotConsole();
                ctx.checkArgs(1, "TPLOC_USAGE");

                String name = ctx.args.get(0);
                Location location = ctx.player.getLocation();

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
                        ctx.returnSuccessLocale("TPLOC_SAVE_SUCCESSFUL");
                    } else {
                        ctx.returnErrorLocale("TPLOC_SAVE_FAILED");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                this.buildCacheList();
            })
            .register();

        new SMCommand("delteleportlocation")
            .alias("delteleportloc", "deltploc")
            .permission("stemcraft.teleport.location.delete")
            .tabComplete("tplocations")
            .action(ctx -> {
                ctx.checkArgs(1, "TPLOC_DELETE_USAGE");

                String name = ctx.args.get(0);
                if (!this.cacheList.containsKey(name)) {
                    ctx.returnErrorLocale("TPLOC_NOT_EXIST", "name", name);
                }

                try {
                    PreparedStatement statement;
                    statement = SMDatabase.prepareStatement(
                        "DELETE FROM tp_locations WHERE name = ?");

                    statement.setString(1, name);

                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        ctx.returnSuccessLocale("TPLOC_DELETE_SUCCESSFUL");
                    } else {
                        ctx.returnErrorLocale("TPLOC_DELETE_FAILED");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                this.buildCacheList();
            })
            .register();

        this.buildCacheList();
        return true;
    }

    private void buildCacheList() {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT name, world, x, y, z, yaw, pitch FROM tp_locations");
            ResultSet resultSet = statement.executeQuery();

            this.cacheList.clear();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String worldName = resultSet.getString("world");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                double z = resultSet.getDouble("z");
                float yaw = resultSet.getFloat("yaw");
                float pitch = resultSet.getFloat("pitch");

                Location location =
                    new Location(STEMCraft.getPlugin().getServer().getWorld(worldName), x, y, z, yaw, pitch);
                this.cacheList.put(name, location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
