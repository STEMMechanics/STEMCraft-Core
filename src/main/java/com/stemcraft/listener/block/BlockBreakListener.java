package com.stemcraft.listener.block;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import com.stemcraft.database.SMDatabase;

public class BlockBreakListener implements Listener {
    
    @EventHandler
    public void BlockBreak(BlockBreakEvent event) {
        
        // Check if waystone
        Block block = event.getBlock();
        if (block.getType() == Material.LODESTONE) {
            try {
                removeWaystone(block);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Block blockAbove = block.getRelative(BlockFace.UP);

            if (blockAbove.getType() == Material.LODESTONE) {
                try {
                    removeWaystone(blockAbove);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void removeWaystone(Block block) throws SQLException {
        PreparedStatement statement = SMDatabase.prepareStatement(
                "DELETE FROM waystones WHERE world = ? AND x = ? AND y = ? AND z = ?"
        );
        statement.setString(1, block.getWorld().getName());
        statement.setInt(2, block.getX());
        statement.setInt(3, block.getY());
        statement.setInt(4, block.getZ());
        statement.executeUpdate();
        statement.close();
    }

}
