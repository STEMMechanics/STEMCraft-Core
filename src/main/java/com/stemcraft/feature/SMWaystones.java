package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.stemcraft.SMUtil;

public class SMWaystones extends SMFeature {
    private List<String> waystoneTypes = Arrays.asList("GOLD_BLOCK", "EMERALD_BLOCK", "DIAMOND_BLOCK");
    private static final Set<String> interactNonce = new HashSet<>();

    @Override
    protected Boolean onEnable() {
        this.plugin.getDatabaseManager().addMigration("230615131000_CreateWaystonesTable", (databaseManager) -> {
            databaseManager.prepareStatement(
            "CREATE TABLE IF NOT EXISTS waystones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "world TEXT NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "under_block TEXT NOT NULL)").executeUpdate();
        });

        this.plugin.getEventManager().registerEvent(BlockBreakEvent.class, (listener, rawEvent) -> {
            BlockBreakEvent event = (BlockBreakEvent) rawEvent;

            Block block = event.getBlock();
            if (block.getType() == Material.LODESTONE) {
                try {
                    this.removeWaystone(block);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Block blockAbove = block.getRelative(BlockFace.UP);

                if (blockAbove.getType() == Material.LODESTONE) {
                    try {
                        this.removeWaystone(blockAbove);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        this.plugin.getEventManager().registerEvent(BlockPlaceEvent.class, (listener, rawEvent) -> {
            BlockPlaceEvent event = (BlockPlaceEvent) rawEvent;

            Block block = event.getBlock();
            if (block.getType() == Material.LODESTONE) {
                try {
                    insertWaystone(block);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Block blockAbove = block.getRelative(BlockFace.UP);

                if (blockAbove.getType() == Material.LODESTONE) {
                    try {
                        insertWaystone(blockAbove);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        this.plugin.getEventManager().registerEvent(PlayerInteractEvent.class, (listener, rawEvent) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) rawEvent;
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock == null) {
                return;
            }
    
            if (!interactNonce.contains(player.getName())) {
                interactNonce.add(player.getName());

                this.plugin.delayedTask(5L, (data) -> {
                    interactNonce.remove(player.getName());
                }, null);

                if(player.getGameMode() == GameMode.SURVIVAL && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (clickedBlock.getType() == Material.LODESTONE && (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().equals(Material.AIR))) {
                        if(this.checkWaystoneExists(clickedBlock.getLocation())) {
                            this.teleportToNearestWaystone(clickedBlock.getLocation(), player);
                        }
                    }
                }
            }
        });

        return true;
    }

    private void removeWaystone(Block block) throws SQLException {
        PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                "DELETE FROM waystones WHERE world = ? AND x = ? AND y = ? AND z = ?"
        );
        statement.setString(1, block.getWorld().getName());
        statement.setInt(2, block.getX());
        statement.setInt(3, block.getY());
        statement.setInt(4, block.getZ());
        statement.executeUpdate();
        statement.close();

        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 2.0f);
    }

    private void insertWaystone(Block block) throws SQLException {
        Block blockBelow = block.getRelative(BlockFace.DOWN);

        String blockBelowName = blockBelow.getType().name();
        if(this.waystoneTypes.contains(blockBelowName)) {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                    "INSERT INTO waystones (world, x, y, z, under_block) VALUES (?, ?, ?, ?, ?)"
            );
            statement.setString(1, block.getWorld().getName());
            statement.setInt(2, block.getX());
            statement.setInt(3, block.getY());
            statement.setInt(4, block.getZ());
            statement.setString(5, blockBelowName);
            statement.executeUpdate();
            statement.close();

            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2.0f);
        }
    }

    public boolean checkWaystoneExists(Location location) {
        try {
            String query = "SELECT COUNT(*) FROM waystones WHERE x = ? AND y = ? AND z = ? AND world = ?";
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(query);
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            statement.setString(4, location.getWorld().getName());
            
            // Execute the query and retrieve the result
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0; // If count > 0, location is registered; otherwise, it is not
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false; // Default return value if there's an error or no result found
    }

    private void teleportToNearestWaystone(Location location, Player player) {
        Block underBlock = location.getBlock().getRelative(BlockFace.DOWN);
        
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Adjust the coordinates by +/- 1000
        int search = 4000;
        int minX = x - search;
        int minY = y - search;
        int minZ = z - search;
        int maxX = x + search;
        int maxY = y + search;
        int maxZ = z + search;
        
        try {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                    "SELECT * FROM waystones WHERE under_block = ? AND world = ? AND x BETWEEN ? AND ? AND y BETWEEN ? AND ? AND z BETWEEN ? AND ?"
            );
            statement.setString(1, underBlock.getType().name());
            statement.setString(2, world.getName());
            statement.setInt(3, minX);
            statement.setInt(4, maxX);
            statement.setInt(5, minY);
            statement.setInt(6, maxY);
            statement.setInt(7, minZ);
            statement.setInt(8, maxZ);
            
            ResultSet resultSet = statement.executeQuery();

            Location closestWaystoneLocation = null;
            double closestDistance = Double.MAX_VALUE;

            while (resultSet.next()) {
                String resultWorldName = resultSet.getString("world");
                int resultX = resultSet.getInt("x");
                int resultY = resultSet.getInt("y");
                int resultZ = resultSet.getInt("z");

                if(!resultWorldName.equalsIgnoreCase(location.getWorld().getName()) || resultX != location.getBlockX() || resultY != location.getBlockY() || resultZ != location.getBlockZ()) {
                    Location waystoneLocation = new Location(Bukkit.getWorld(resultWorldName), resultX, resultY, resultZ);
                    double distance = waystoneLocation.distance(location);
                    if (distance < closestDistance) {
                        closestWaystoneLocation = waystoneLocation;
                        closestDistance = distance;
                    }
                }
            }

            if (closestWaystoneLocation != null) {
                // Teleport the player to a safe location near the closest waystone
                Location safeLocation = SMUtil.findSafeLocation(closestWaystoneLocation, 6, true);
                if (safeLocation != null) {
                    this.plugin.delayedTask(1L, (data) -> {
                        location.getWorld().playSound(location, Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, 1f, 0.5f);
                        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 3f);
                        player.teleport(safeLocation);
                        location.getWorld().playSound(safeLocation, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 3f);
                        location.getWorld().playSound(safeLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    }, null);
                } else {
                    player.sendMessage("Unable to find a safe location near the waystone");
                }
            } else {
                player.sendMessage("No waystones found nearby");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
