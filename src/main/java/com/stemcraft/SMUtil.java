package com.stemcraft;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SMUtil {
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
        Block belowBlock = block.getRelative(BlockFace.DOWN);
        
        // Check if the block and the block above are air blocks
        if (block.getType() == Material.AIR && aboveBlock.getType() == Material.AIR && belowBlock.getType().isSolid()) {
            // // Check if the block and the block above are not water or lava blocks
            // if (block.getType() != Material.WATER && block.getType() != Material.LAVA &&
            //         aboveBlock.getType() != Material.WATER && aboveBlock.getType() != Material.LAVA) {
            //     // Check if the block and the block above are not solid blocks (excluding certain transparent blocks)
            //     if (!block.getType().isSolid() && !aboveBlock.getType().isSolid()) {
                    return true;
            //     }
            // }
        }

        return false;
    }

    public static Location findSafeLocation(Location location, int range) {
        return findSafeLocation(location, range, false);
    }
    
    public static Location findSafeLocation(Location location, int range, boolean random) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        List<Location> safeLocations = new ArrayList<>();

        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {
                for (int k = -range; k <= range; k++) {
                    Location checkLocation = new Location(world, x + i, y + j, z + k);
                    if (isSafeLocation(checkLocation)) {
                        if(!random) {
                            return checkLocation;
                        }

                        safeLocations.add(checkLocation);
                    }
                }
            }
        }

        if (!safeLocations.isEmpty()) {
            Random randomLoc = new Random();
            int randomIndex = randomLoc.nextInt(safeLocations.size());
            return safeLocations.get(randomIndex);
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

    public static boolean loadChunkRadius(Location pos, int radius, boolean force) {
        int baseX = (int)(pos.getX() / 16.0D);
        int baseZ = (int)(pos.getZ() / 16.0D);
        boolean success = true;

        for(int x = -1 * radius; x <= radius; x++) {
            for(int z = -1 * radius; z <= radius; z++) {
                int chunkX = baseX + x;
                int chunkZ = baseZ + z;
                World world = pos.getWorld();

                try {
                    world.addPluginChunkTicket(chunkX, chunkZ, STEMCraft.getInstance());
                }
                catch (NoSuchMethodError e) {
                    /* empty */
                }

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                if(!world.isChunkLoaded(chunk)) {
                    boolean load = false;

                    try {
                        load = world.loadChunk(chunkX, chunkZ, true);
                    } catch (RuntimeException e) {
                        load = false;
                    }

                    if(!load) {
                        if(success) {
                            success = false;
                        }
                        STEMCraft.getInstance().getLogger().warning("Failed to load and activate Chunk at X: " + (chunkX * 16) + " Z: " + (chunkZ * 16) + " in " + world.getName());
                    }
                }
            }
        }

        return success;
    }

    public static void paginate(CommandSender sender, String title, List<BaseComponent[]> content, int page, int maxPages, String command) {
        // Title
        sender.spigot().sendMessage(createSeperatorString(ChatColor.AQUA + title));
        
        // Display the content for the current page
        for (int i = 0; i < content.size(); i++) {
            sender.spigot().sendMessage(content.get(i));
        }

        // Pagination
        BaseComponent prev = new TextComponent((page <= 1 ? ChatColor.GRAY : ChatColor.GOLD) + "<<< ");
        if(page > 1) {
            prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (page - 1)));
            prev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Previous page")));
        }

        BaseComponent pageInfo = new TextComponent(ChatColor.YELLOW + "Page " + ChatColor.GOLD + page + ChatColor.YELLOW + " of " + maxPages);

        BaseComponent next = new TextComponent((page >= maxPages ? ChatColor.GRAY : ChatColor.GOLD) + " >>>");
        if(page < maxPages) {
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (page + 1)));
            next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Next page")));
        }

        BaseComponent components[] = { prev, pageInfo, next };
        sender.spigot().sendMessage(createSeperatorString(components));
    }

    private static BaseComponent[] createSeperatorString(String title) {
        TextComponent textComponent = new TextComponent(title);
        return createSeperatorString(new BaseComponent[]{textComponent});
    }

    private static BaseComponent[] createSeperatorString(BaseComponent[] titles) {
        StringBuilder plainTitleBuilder = new StringBuilder();
        for (BaseComponent title : titles) {
            plainTitleBuilder.append(title.toPlainText());
        }
        String plainTitle = plainTitleBuilder.toString();
        String separator = "-";

        int maxLength = 58;
        int titleLength = ChatColor.stripColor(plainTitle).length();
        int separatorLength = (maxLength - titleLength - 4) / 2;

        String separatorStr = ChatColor.YELLOW + separator.repeat(separatorLength) + ChatColor.RESET;

        BaseComponent[] components = {
                new TextComponent(separatorStr + " "),
                new TextComponent(titles),
                new TextComponent(" " + separatorStr)
        };

        return components;
    }

    public static <K, V> void insertAfter(LinkedHashMap<K, V> map, K currentKey, K newKey, V newValue) {
        boolean found = false;
        LinkedHashMap<K, V> newMap = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());

            if (entry.getKey().equals(currentKey)) {
                newMap.put(newKey, newValue);
                found = true;
            }
        }

        if (!found) {
            // If currentKey was not found, append the newKey/newValue to the end
            newMap.put(newKey, newValue);
        }

        map.clear();
        map.putAll(newMap);
    }
}
