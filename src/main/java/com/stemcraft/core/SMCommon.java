package com.stemcraft.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.stemcraft.STEMCraft;
import lombok.NonNull;
import static org.bukkit.ChatColor.COLOR_CHAR;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMCommon {
    
    /*
     * Regex for finding color codes
     */
    private static final Pattern COLOR_PATTERN = Pattern.compile("((&|" + COLOR_CHAR + ")[0-9a-fk-or])|(" + COLOR_CHAR + "x(" + COLOR_CHAR + "[0-9a-fA-F]){6})|((?<!\\\\)(\\{|&|)#((?:[0-9a-fA-F]{3}){2})(}|))");

    /** Pattern matching "nicer" legacy hex chat color codes - &#rrggbb */
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    
    /**
     * Strip color codes from string.
     * @param message
     * @return
     */
    public static String stripColors(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Matcher matcher = COLOR_PATTERN.matcher(message);

        while (matcher.find()) {
            message = matcher.replaceAll("");
        }

        return message;
    }

    /**
     * Colourize a string.
     * @param message
     * @return
     */
    public static String colorize(String message) {
        // Convert from the '&#rrggbb' hex color format to the '&x&r&r&g&g&b&b' one used by Bukkit.
        Matcher matcher = HEX_COLOR_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder(14).append("&x");
            for (char character : matcher.group(1).toCharArray()) {
                replacement.append('&').append(character);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    /**
     * Colourize a string array.
     * @param messages
     * @return
     */
    public static String[] colorizeAll(String... messages) {
        String[] colorizedMessages = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            colorizedMessages[i] = colorize(messages[i]);
        }
        return colorizedMessages;
    }



    /**
     * Retrieve a player by name or UUID.
     * @param nameOrUUID
     * @return
     */
    public static Player findPlayer(String nameOrUUID) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().toString().equalsIgnoreCase(nameOrUUID) || player.getDisplayName().equalsIgnoreCase(nameOrUUID)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Is the specified location safe for a player to spawn
     * @param location
     * @return
     */
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

    /**
     * Find the first safe location within the range from the specified location.
     * @param location
     * @param range
     * @return
     */
    public static Location findSafeLocation(Location location, int range) {
        return findSafeLocation(location, range, false);
    }
    
    /**
     * Find the first or random safe location within the range from the specified location.
     * @param location
     * @param range
     * @param random
     * @return
     */
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

    /**
     * Equivalent to {@link String#replace(CharSequence, CharSequence)}, but uses a
     * {@link Supplier} for the replacement.
     *
     * @param pattern the pattern for the replacement target
     * @param input the input string
     * @param replacement the replacement
     * @return the input string with the replacements applied
     */
    public static String replaceAll(Pattern pattern, String input, Supplier<String> replacement) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.replaceAll(Matcher.quoteReplacement(replacement.get()));
        }
        return input;
    }

    /**
     * Add items to a List<String>
     * @param list
     * @param messages
     */
    public static void append(List<String> list, String... messages) {
		list.addAll(Arrays.asList(messages));
	}

    /**
	 * Returns the value if it is not {@code null}. Otherwise, the default value is returned.
	 * If the value is a {@link String}, the default value is returned if the value is empty.
	 *
	 * @param value        the value to be checked.
	 * @param defaultValue the default value to be returned if the actual value is {@code null}.
	 * @param <T>          the type of the value and default value.
	 * @return the value if it is not {@code null}, otherwise the default value.
	 */
	public static <T> T getOrDefault(final T value, final T defaultValue) {
		if (value instanceof String && "".equals(value)) {
			return defaultValue;
        }

		return value != null ? value : defaultValue;
	}

    public static String capitalize(String str) {
        return capitalize(str, false);
    }

    public static String capitalize(String str, Boolean ignoreColors) {
		if (str != null && str.length() != 0) {
			final int strLen = str.length();
			final StringBuffer buffer = new StringBuffer(strLen);
			boolean capitalizeNext = true;

			for (int i = 0; i < strLen; ++i) {
				final char ch = str.charAt(i);

				if (Character.isWhitespace(ch)) {
					buffer.append(ch);

					capitalizeNext = true;
                } else if (ch == '&' && ignoreColors && i + 1 < strLen && "0123456789abcdefklmnor".indexOf(str.charAt(i + 1)) != -1) {
                    buffer.append(ch).append(str.charAt(i + 1));
                    i++;

                } else if (capitalizeNext) {
					buffer.append(Character.toTitleCase(ch));

					capitalizeNext = false;
				} else {
					buffer.append(ch);
                }
			}

			return buffer.toString();
		}

		return str;
    }
    
    public static String beautify(String str) {
        return str.toLowerCase().replace("_", " ");
    }

    public static String beautifyCapitalize(String str) {
        return capitalize(beautify(str));
    }

    public static String beautifyCapitalize(@NonNull Enum<?> enumeration) {
		return beautifyCapitalize(enumeration.toString().toLowerCase());
    }

    public static HashMap<String, Object> mapOfArray(final Object... array) {
        HashMap<String, Object> map = new HashMap<>();

        if(array != null) {
            for (int i = 0; i < array.length; i += 2) {
                String key = array[i].toString();
                Object value = null;
                
                if (i + 1 < array.length) {
                    value = array[i + 1];
                }
                
                map.put(key, value);
            }
        }

        return map;
    }

    public static Boolean itemIsRepairable(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        return (itemMeta instanceof Damageable);
    }

    public static ItemStack repairItem(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable)itemMeta;
            damageable.setDamage(0);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    public static void delayedPlayerTeleport(Player player, Location location) {
        STEMCraft.runLater(1, () -> {
            player.teleport(location);
        });
    }

    public static String getKeyByValue(Map<String, String> map, String value) {
        return getKeyByValue(map, value, null);
    }
    
    public static String getKeyByValue(Map<String, String> map, String value, String defaultValue) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return defaultValue;
    }

    public static String format(String str) {
        return colorize(str);
    }

    /**
	 * Joins multiple arrays into one array.
	 *
	 * @param arrays the arrays to be joined.
	 * @param <T>    the type of elements in the arrays.
	 * @return an array containing all the elements from the input arrays.
	 */
	@SafeVarargs
	public static <T> Object[] joinArrays(final T[]... arrays) {
		final List<T> all = new ArrayList<>();

		for (final T[] array : arrays)
			all.addAll(Arrays.asList(array));

		return all.toArray();
	}

	/**
	 * Joins multiple {@link Iterable lists} into one {@link List}.
	 *
	 * @param lists the {@link Iterable lists} to be joined.
	 * @param <T>   the type of elements in the lists.
	 * @return a {@link List} containing all the elements from the input {@link Iterable lists}.
	 */
	@SafeVarargs
	public static <T> List<T> joinLists(final Iterable<T>... lists) {
		final List<T> all = new ArrayList<>();

		for (final Iterable<T> array : lists)
			for (final T element : array)
				all.add(element);

		return all;
	}

    /**
	 * Joins elements from the input array, separated by ", ". We invoke {@link T#toString()} for each element given it
	 * is not {@code null}, or return an empty {@link String} if it is.
	 *
	 * @param array the input array.
	 * @param <T>   the type of elements in the array.
	 * @return a {@link String} containing the joined elements of the array.
	 */
	public static <T> String join(final T[] array) {
		return array == null ? "null" : join(Arrays.asList(array));
	}

	/**
	 * Joins elements from the input {@link Iterable array}, separated by ", ". We invoke {@link T#toString()} for each
	 * element given it is not {@code null}, or return an empty {@link String} if it is.
	 *
	 * @param array the input {@link Iterable array}.
	 * @param <T>   the type of elements in the array.
	 * @return a {@link String} containing the joined elements of the iterable.
	 */
	public static <T> String join(final Iterable<T> array) {
		return array == null ? "null" : join(array, ", ");
	}

	/**
	 * Joins elements from the input array, separated by the specified delimiter. We invoke {@link T#toString()} for
	 * each element given it is not null, or return an empty {@link String} if it is.
	 *
	 * @param array     the input array.
	 * @param delimiter the delimiter used to separate the joined elements.
	 * @param <T>       the type of elements in the array.
	 * @return a {@link String} containing the joined elements of the array with the specified delimiter.
	 */
	public static <T> String join(final T[] array, final String delimiter) {
		return join(array, delimiter);
	}

	/**
	 * Joins elements from the input {@link Iterable array}, separated by the specified delimiter. We invoke
	 * {@link T#toString()} for each element given it is not null, or return an empty {@link String} if it is.
	 *
	 * @param array     the input {@link Iterable array}.
	 * @param delimiter the delimiter used to separate the joined elements.
	 * @param <T>       the type of elements in the array.
	 * @return a {@link String} containing the joined elements of the {@link Iterable array} with the specified
	 * delimiter.
	 */
	public static <T> String join(final Iterable<T> array, final String delimiter) {
		return join(array, delimiter);
	}

    /**
     * Is player holding a tool with silk touch?
     * 
     * @param Player the player.
     * @return if it is true.
     */
    public static Boolean playerHasSilkTouch(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            if (itemInHand.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                return true;
            }
        }

        return false;
    }
}
