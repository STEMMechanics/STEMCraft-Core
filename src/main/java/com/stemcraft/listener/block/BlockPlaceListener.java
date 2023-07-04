package com.stemcraft.listener.block;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import com.stemcraft.component.ComponentLockdown;
import com.stemcraft.component.ComponentWaystone;
import com.stemcraft.database.SMDatabase;

public class BlockPlaceListener implements Listener {
    
    @EventHandler
    public void BlockPlace(BlockPlaceEvent event) {
        if (ComponentLockdown.blockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.YELLOW + "You are required to enter the login code before you can interact");
            return;
        }

        // Check if waystone
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
    }

    private void insertWaystone(Block block) throws SQLException {
        Block blockBelow = block.getRelative(BlockFace.DOWN);

        String blockBelowName = blockBelow.getType().name();
        if(ComponentWaystone.waystoneTypes.contains(blockBelowName)) {
            PreparedStatement statement = SMDatabase.prepareStatement(
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

}
