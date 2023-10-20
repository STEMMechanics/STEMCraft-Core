package com.stemcraft.core.serialize;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import com.stemcraft.core.SMValid;
import com.stemcraft.core.exception.SMInvalidWorldException;

public class SMSerializeLocation {

    /**
     * Converts a Location into a "world x y z pitch yaw" string
     *
     * @param effect
     * @return
     */
    public static String serialize(final Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + (location.getPitch() != 0F || location.getYaw() != 0F ? " " + Math.round(location.getYaw()) + " " + Math.round(location.getPitch()) : "");
    }

    /**
     * Converts a String "world x y z pitch yaw" into a Location
     *
     * @param effect
     * @return
     */
    public static Location deserialize(final String raw) {
        if (raw == null)
            return null;

        final String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
        SMValid.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

        final String world = parts[0];
        final World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null)
            throw new SMInvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);

        final int x = Integer.parseInt(parts[1]), y = Integer.parseInt(parts[2]), z = Integer.parseInt(parts[3]);
        final float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0"), pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");

        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }
}
