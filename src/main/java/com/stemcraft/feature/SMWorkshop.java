package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;
import com.stemcraft.core.util.SMWorldRegion;

public class SMWorkshop extends SMFeature {
    @Override
    protected Boolean onEnable() {
        SMTabComplete.register("workshops", () -> {
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

        new SMCommand("workshop")
            .tabComplete("{workshops}")
            .action(ctx -> {
                ctx.checkNotConsole();
                ctx.checkArgs(1, "WORKSHOP_USAGE");

                SMWorldRegion worldRegion = this.findWorkshopRegion(ctx.args[0]);
                if(worldRegion == null) {
                    World world = Bukkit.getWorld("workshop_" + ctx.args[0]);
                    if(world == null) {
                        ctx.returnErrorLocale("WORKSHOP_NOT_FOUND");
                    } else {
                        SMCommon.delayedPlayerTeleport(ctx.player, world.getSpawnLocation());
                    }
                } else {
                    Location center = worldRegion.center();
                    SMCommon.delayedPlayerTeleport(ctx.player, center);
                }

                ctx.returnSuccessLocale("WORKSHOP_ENTER");
            })
            .register();

        return true;
    }

    public SMWorldRegion findWorkshopRegion(String searchString) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            
            if (regionManager != null) {
                for (ProtectedRegion region : regionManager.getRegions().values()) {
                    if (region.getId().matches("workshop_" + searchString)) {
                        return new SMWorldRegion(region, world);
                    }
                }
            }
        }

        return null; // No matching region found
    }
}
