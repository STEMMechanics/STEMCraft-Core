package com.stemcraft.feature;

import java.util.HashMap;
import java.util.Map;

import com.stemcraft.core.SMReplacer;
import com.stemcraft.core.config.SMConfig;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.event.SMEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * A trader villager that spawns around the world.
 */
public class SMCoordinates extends SMFeature {
    /**
     * This class represents the player coordinate data.
     */
    public static class CoordData {
        public BossBar bossBar = null;
        public Boolean actionBar = false;

        // Constructor
        public CoordData() {
            this.bossBar = null;
            this.actionBar = false;
        }

        // Constructor
        public CoordData(BossBar bossBar, Boolean actionBar) {
            this.bossBar = bossBar;
            this.actionBar = actionBar;
        }
    }

    /**
     * A list of CoordData per player (if enabled).
     */
    private static final Map<Player, CoordData> coordBars = new HashMap<>();

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {

        SMEvent.register(PlayerQuitEvent.class, ctx -> {
            removeCoordBars(ctx.event.getPlayer());
        });

        new SMCommand("coord")
            .permission("stemcraft.command.coord")
            .action(ctx -> {
                ctx.checkNotConsole();
                toggleActionBar(ctx.player);
            }).register();

        new SMCommand("coordbar")
            .permission("stemcraft.command.coordbar")
            .action(ctx -> {
                ctx.checkNotConsole();
                toggleBossBar(ctx.player);
            }).register();


        STEMCraft.runTimer(5, () -> {
            for (Player player : coordBars.keySet()) {
                if (!player.isOnline()) {
                    removeCoordBars(player);
                    continue;
                }

                CoordData coordData = coordBars.get(player);
                if (coordData.bossBar == null && !coordData.actionBar) {
                    return;
                }

                String world = SMCommon.beautifyCapitalize(player.getLocation().getWorld().getName());
                String time = SMCommon.convertWorldToRealTime(player.getLocation().getWorld());
                String direction = SMCommon.getCompassDirection(player.getLocation().getYaw());

                if (coordData.bossBar != null) {
                    String coordBarString = SMConfig.main().getString("coord.boss-bar", ":world: {world} :clock: {time} :compass: {direction}");
                    coordBarString = SMReplacer.replaceVariables(
                            coordBarString,
                            "world",
                            world,
                            "time",
                            time,
                            "direction",
                            direction,
                            "x", String.valueOf(player.getLocation().getBlockX()),
                            "y", String.valueOf(player.getLocation().getBlockY()),
                            "z", String.valueOf(player.getLocation().getBlockZ())
                    );

                    coordData.bossBar.setTitle(
                        SMBridge.parse(coordBarString));
                }

                if (coordData.actionBar) {
                    String coordString = SMConfig.main().getString("coord.action-bar", "&6XYZ: &f{x} {y} {z}  &6{direction}      {time}");
                    coordString = SMReplacer.replaceVariables(
                            coordString,
                            "world",
                            world,
                            "time",
                            time,
                            "direction",
                            direction,
                            "x", String.valueOf(player.getLocation().getBlockX()),
                            "y", String.valueOf(player.getLocation().getBlockY()),
                            "z", String.valueOf(player.getLocation().getBlockZ())
                    );

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(SMCommon.colorize(coordString)));
                }
            }
        });

        return true;
    }

    /**
     * Add a boss bar to a player.
     * 
     * @param player The player to add the bar.
     */
    private static void addBossBar(Player player) {
        if (!coordBars.containsKey(player)) {
            coordBars.put(player, new CoordData(null, false));
        }

        if (coordBars.get(player).bossBar == null) {
            BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
            bossBar.addPlayer(player);
            bossBar.setVisible(true);
            coordBars.get(player).bossBar = bossBar;
        }
    }

    /**
     * Add a action bar to a player.
     * 
     * @param player The player to add the bar.
     */
    private static void addActionBar(Player player) {
        if (!coordBars.containsKey(player)) {
            coordBars.put(player, new CoordData(null, true));
        } else {
            coordBars.get(player).actionBar = true;
        }
    }

    /**
     * Remove a boss bar from a player.
     * 
     * @param player The player to remove the bar.
     */
    private static void removeBossBar(Player player) {
        if (coordBars.containsKey(player)) {
            CoordData bars = coordBars.get(player);

            if (bars.bossBar != null) {
                bars.bossBar.removeAll();
                bars.bossBar = null;
            }
        }
    }

    /**
     * Remove a action bar from a player.
     * 
     * @param player The player to remove the bar.
     */
    private static void removeActionBar(Player player) {
        if (coordBars.containsKey(player)) {
            coordBars.get(player).actionBar = false;
        }
    }

    /**
     * Toggle the boss bar of a player.
     * 
     * @param player The player to toggle the bar.
     */
    private static void toggleBossBar(Player player) {
        if (coordBars.containsKey(player)) {
            if (coordBars.get(player).bossBar != null) {
                removeBossBar(player);
                return;
            }
        }

        addBossBar(player);
    }

    /**
     * Toggle the action bar of a player.
     * 
     * @param player The player to toggle the bar.
     */
    private static void toggleActionBar(Player player) {
        if (coordBars.containsKey(player)) {
            if (coordBars.get(player).actionBar) {
                removeActionBar(player);
                return;
            }
        }

        addActionBar(player);
    }

    /**
     * Remove all coords bars from the player.
     * 
     * @param player The player to remove.
     */
    private static void removeCoordBars(Player player) {
        if (coordBars.containsKey(player)) {
            CoordData bars = coordBars.get(player);

            if (bars.bossBar != null) {
                bars.bossBar.removeAll();
                bars.bossBar = null;
            }

            bars.actionBar = false;
            coordBars.remove(player);
        }
    }
}
