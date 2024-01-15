package com.stemcraft.feature;

import java.util.function.Consumer;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

/**
 * Allows the player to open workbenches on command
 */
public class SMWorldGuard extends SMFeature {
    public static class Flags {
        public static StateFlag WORLD_DROPS_FLAG;
    }


    /**
     * When feature is loaded
     */
    @Override
    public Boolean onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            StateFlag flag = new StateFlag("world-drops", true);
            registry.register(flag);
            Flags.WORLD_DROPS_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("world-drops");
            if (existing instanceof StateFlag) {
                Flags.WORLD_DROPS_FLAG = (StateFlag) existing;
            } else {
                STEMCraft.severe("world-drops flag has already been registered in WorldGuard");
            }
        }

        return true;
    }

    @Override
    public Boolean onEnable() {
        SMEvent.register(PlayerDropItemEvent.class, ctx -> {
            testFlag(ctx.event.getPlayer().getLocation(), ctx.event, Flags.WORLD_DROPS_FLAG, e -> {
                e.setCancelled(true);
            });
        });

        SMEvent.register(ItemSpawnEvent.class, ctx -> {
            testFlag(ctx.event.getEntity().getLocation(), ctx.event, Flags.WORLD_DROPS_FLAG, e -> {
                e.setCancelled(true);
            });
        });

        SMEvent.register(EntityDeathEvent.class, ctx -> {
            testFlag(ctx.event.getEntity().getLocation(), ctx.event, Flags.WORLD_DROPS_FLAG, e -> {
                e.getDrops().clear();
            });
        });

        SMEvent.register(EntityExplodeEvent.class, ctx -> {
            testFlag(ctx.event.getEntity().getLocation(), ctx.event, Flags.WORLD_DROPS_FLAG, e -> {
                if (e.getEntity() instanceof TNTPrimed) {
                    e.setYield(0);
                }
            });
        });

        return true;
    }

    private static <T extends Event> void testFlag(org.bukkit.Location location, T event, StateFlag flag,
        Consumer<T> callback) {
        Location weLocation = BukkitAdapter.adapt(location);
        World weWorld = BukkitAdapter.adapt(location.getWorld());

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(weWorld);

        if (regions != null) {
            ApplicableRegionSet set = regions.getApplicableRegions(weLocation.toVector().toBlockPoint());
            if (!set.testState(null, flag)) {
                callback.accept(event);
            }
        }
    }
}
