package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMReplacer;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.core.util.SMWorldRegion;

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
        SMDatabase.runMigration("240115112900_CreateRegionTemplateTable", () -> {
            SMDatabase.prepareStatement(
                "CREATE TABLE IF NOT EXISTS rg_templates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "world TEXT," +
                    "template TEXT)")
                .executeUpdate();
        });

        SMEvent.register(PlayerCommandPreprocessEvent.class, ctx -> {
            String command = ctx.event.getMessage().toLowerCase();

            // Check if the command starts with /rg del, /rg delete, /rg rem, or /rg remove
            if (command.startsWith("/rg del") || command.startsWith("/rg delete") ||
                command.startsWith("/rg rem") || command.startsWith("/rg remove")) {

                STEMCraft.runLater(10, () -> {
                    runRemovedRegionTemplateCommands();
                });
            }
        });

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

        new SMCommand("rgtemplate")
            .action(ctx -> {

                String id = ctx.getArg(1);
                String templateName = "";
                org.bukkit.World world = null;

                if (id.length() == 0) {
                    ctx.returnErrorLocale("RG_TEMPLATE_USAGE");
                }

                for (int idx = 2; idx <= ctx.args.size(); idx++) {
                    if (ctx.getArg(idx) == "-w") {
                        String worldName = ctx.getArg(idx + 1);
                        if (worldName.length() == 0) {
                            ctx.returnErrorLocale("RG_TEMPLATE_WORLD_MISSING");
                        }

                        world = Bukkit.getServer().getWorld(worldName);
                        if (world == null) {
                            ctx.returnErrorLocale("RG_TEMPLATE_WORLD_NOT_FOUND", "world", worldName);
                        }

                        idx++;
                    } else if (templateName.length() == 0) {
                        templateName = ctx.getArg(idx);
                    }
                }

                if (world == null) {
                    if (ctx.fromConsole()) {
                        ctx.returnErrorLocale("RG_TEMPLATE_WORLD_REQUIRED_CONSOLE");
                    } else {
                        world = ctx.player.getLocation().getWorld();
                    }
                }

                if (!getWorldRegions(world).contains(id)) {
                    ctx.returnErrorLocale("RG_TEMPLATE_REGION_NOT_FOUND");
                }

                List<String> templateNames = SMConfig.main().getKeys("region_templates");
                if (!templateNames.contains(templateName)) {
                    ctx.returnErrorLocale("RG_TEMPLATE_NOT_FOUND", "template", templateName);
                }

                String workshopName = id.substring(9);
                String beautifiedName = SMCommon.beautifyCapitalize(workshopName);
                CommandSender sender = Bukkit.getConsoleSender();

                for (String flag : SMConfig.main().getKeys("region_templates." + templateName + ".flags")) {
                    String value = SMConfig.main().getString("region_templates." + templateName + ".flags." + flag);

                    value =
                        SMReplacer.replaceVariables(value, "id", id, "world", world.getName(), "title", beautifiedName);

                    String cmd = "rg flag " + templateName + " " + flag + " " + value;
                    Bukkit.dispatchCommand(sender, cmd);
                }

                for (String cmd : SMConfig.main()
                    .getStringList("region_templates." + templateName + ".commands.create")) {
                    cmd =
                        SMReplacer.replaceVariables(cmd, "id", id, "world", world.getName(), "title", beautifiedName);
                    Bukkit.dispatchCommand(sender, cmd);
                }
            })
            .register();


        return true;
    }

    public List<String> getWorldRegions(org.bukkit.World world) {
        List<String> regions = new ArrayList<>();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        if (regionManager != null) {
            for (ProtectedRegion region : regionManager.getRegions().values()) {
                regions.add(region.getId());
            }
        }

        return regions;
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
