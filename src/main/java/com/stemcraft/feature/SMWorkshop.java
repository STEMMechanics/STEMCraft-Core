package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class SMWorkshop extends SMFeature {
    class RegionAndWorld {
        private final ProtectedRegion region;
        private final World world;
    
        public RegionAndWorld() {
            this.region = null;
            this.world = null;
        }

        public RegionAndWorld(ProtectedRegion region, World world) {
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

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("WORKSHOP_ENTER", ":info_blue: &bYou have been teleported to the workshop");
        this.plugin.getLanguageManager().registerPhrase("WORKSHOP_NOT_FOUND", ":warning_red: &cWorkshop not found");

        this.plugin.getCommandManager().registerTabPlaceholder("workshops", (Server server, String match) -> {
            List<String> list = new ArrayList<>();

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            for (World world : Bukkit.getWorlds()) {
                if (world.getName().startsWith("workshop_")) {
                    list.add(world.getName().substring(9));
                }
                
                RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
                
                if (regionManager != null) {
                    for (ProtectedRegion region : regionManager.getRegions().values()) {
                        if (region.getId().startsWith("workshop_")) {
                            list.add(region.getId().substring(9));
                        }
                    }
                }
            }

            return list;
        });

        String[] aliases = new String[]{};
        String[][] tabCompletions = new String[][]{
            {"workshop", "%workshops%"},
        };

        this.plugin.getCommandManager().registerCommand("workshop", (sender, command, label, args) -> {
            Player player = (Player)sender;

            RegionAndWorld regionAndWorld = this.findWorkshopRegion(args[0]);
            if(regionAndWorld == null) {
                World world = Bukkit.getWorld("workshop_" + args[0]);
                if(world == null) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "WORKSHOP_NOT_FOUND");
                    return true;
                } else {
                    player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
                }
            } else {
                Location center = regionAndWorld.center();
                player.teleport(center, TeleportCause.PLUGIN);
            }

            this.plugin.getLanguageManager().sendPhrase(sender, "WORKSHOP_ENTER");
            return true;
        }, aliases, tabCompletions);

        return true;
    }

    public RegionAndWorld findWorkshopRegion(String searchString) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            
            if (regionManager != null) {
                for (ProtectedRegion region : regionManager.getRegions().values()) {
                    if (region.getId().matches("workshop_" + searchString)) {
                        return new RegionAndWorld(region, world);
                    }
                }
            }
        }

        return null; // No matching region found
    }
}
