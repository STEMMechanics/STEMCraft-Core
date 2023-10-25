package com.stemcraft.core.util;

import org.bukkit.Location;
import org.bukkit.World;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SMWorldRegion {
    private final ProtectedRegion region;
    private final World world;

    public SMWorldRegion() {
        this.region = null;
        this.world = null;
    }

    public SMWorldRegion(ProtectedRegion region, World world) {
        this.region = region;
        this.world = world;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public World getWorld() {
        return world;
    }

    public Location center() {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        
        double centerX = (min.getX() + max.getX()) / 2;
        double centerY = (min.getY() + max.getY()) / 2;
        double centerZ = (min.getZ() + max.getZ()) / 2;
        
        return new Location(world, centerX, centerY, centerZ);
    }
}
