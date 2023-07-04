package com.stemcraft.listener.player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import com.stemcraft.component.ComponentLockdown;
import com.stemcraft.database.SMDatabase;
import com.stemcraft.utility.Util;

public class PlayerInteractListener implements Listener {
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (ComponentLockdown.blockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "You are required to enter the login code before you can interact");
            return;
        }

        if (clickedBlock == null) {
            return;
        }

        // Check for waystone
        if (clickedBlock.getType() == Material.LODESTONE) {
            if(checkWaystoneExists(clickedBlock.getLocation())) {
                teleportToNearestWaystone(clickedBlock.getLocation(), player);
            }
        }
    }

    public boolean checkWaystoneExists(Location location) {
        try {
            String query = "SELECT COUNT(*) FROM your_table WHERE location_x = ? AND location_y = ? AND location_z = ?";
            PreparedStatement statement = SMDatabase.prepareStatement(query);
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            
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
        int minX = x - 1000;
        int minY = y - 1000;
        int minZ = z - 1000;
        int maxX = x + 1000;
        int maxY = y + 1000;
        int maxZ = z + 1000;
        
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
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

                Location waystoneLocation = new Location(Bukkit.getWorld(resultWorldName), resultX, resultY, resultZ);
                double distance = waystoneLocation.distance(location);
                if (distance < closestDistance) {
                    closestWaystoneLocation = waystoneLocation;
                    closestDistance = distance;
                }
            }

            if (closestWaystoneLocation != null) {
                // Teleport the player to a safe location near the closest waystone
                Location safeLocation = Util.findSafeLocation(closestWaystoneLocation, 6);
                if (safeLocation != null) {
                    player.teleport(safeLocation);
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
