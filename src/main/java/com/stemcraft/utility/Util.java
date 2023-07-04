package com.stemcraft.utility;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Util {
    public static String capitalize(String string, boolean allWords) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        if (string.length() <= 1) {
            return string.toUpperCase(Locale.ROOT);
        }

        string = string.toLowerCase(Locale.ROOT);

        if (allWords) {
            StringBuilder builder = new StringBuilder();
            for (String substring : string.split(" ")) {
                if (substring.length() >= 3 && !substring.equals("and") && !substring.equals("the")) {
                    substring = substring.substring(0, 1).toUpperCase(Locale.ROOT) + substring.substring(1);
                }
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(substring);
            }

            return builder.toString();
        }

        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
    }

    public static boolean isSafeLocation(Location location) {
        Block block = location.getBlock();
        Block aboveBlock = block.getRelative(BlockFace.UP);
        
        // Check if the block and the block above are air blocks
        if (block.getType() == Material.AIR && aboveBlock.getType() == Material.AIR) {
            // Check if the block and the block above are not water or lava blocks
            if (block.getType() != Material.WATER && block.getType() != Material.LAVA &&
                    aboveBlock.getType() != Material.WATER && aboveBlock.getType() != Material.LAVA) {
                // Check if the block and the block above are not solid blocks (excluding certain transparent blocks)
                if (!block.getType().isSolid() && !aboveBlock.getType().isSolid()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Location findSafeLocation(Location location, int range) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {
                for (int k = -range; k <= range; k++) {
                    Location checkLocation = new Location(world, x + i, y + j, z + k);
                    if (isSafeLocation(checkLocation)) {
                        return checkLocation;
                    }
                }
            }
        }

        return null;
    }

    public static Player getPlayerByDisplayName(String displayName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getDisplayName().equalsIgnoreCase(displayName)) {
                return player;
            }
        }
        return null;
    }

}
