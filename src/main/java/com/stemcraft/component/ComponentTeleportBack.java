package com.stemcraft.component;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;

public class ComponentTeleportBack extends SMComponent {
    public final static HashMap<UUID, Location> playerPreviousLocations = new HashMap<>();
}
