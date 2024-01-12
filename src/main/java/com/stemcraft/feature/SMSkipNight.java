package com.stemcraft.feature;

import java.util.HashMap;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.stemcraft.core.event.SMEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.SMReplacer;
import com.stemcraft.core.config.SMConfig;

public class SMSkipNight extends SMFeature {
    private float skipPercentange = 1f;
    private int skipRandomTickSpeed = 3;
    private HashMap<World, BossBar> worlds = new HashMap<>();
    private HashMap<World, Integer> worldRandomTickCount = new HashMap<>();

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        skipPercentange = SMConfig.main().getFloat("skip-night.required", 1f);
        skipRandomTickSpeed = SMConfig.main().getInt("skip-night.random-tick-speed", 300);
        List<String> worldsList = SMConfig.main().getStringList("skip-night.worlds");

        worldsList.forEach(worldName -> {
            World world = Bukkit.getServer().getWorld(worldName);
            if (world != null) {
                worlds.put(world, null);
            }
        });

        /*
         * PlayerBedEnterEvent
         */
        SMEvent.register(PlayerBedEnterEvent.class, (ctx) -> {
            PlayerBedEnterEvent event = (PlayerBedEnterEvent) ctx.event;
            Player player = event.getPlayer();
            World world = player.getLocation().getWorld();

            if (player.getGameMode() == GameMode.SURVIVAL && worlds.containsKey(world)) {
                STEMCraft.runLater(1, () -> {
                    updateSleepers(world);
                });
            }
        });

        /*
         * PlayerJoinEvent
         */
        SMEvent.register(PlayerJoinEvent.class, (ctx) -> {
            STEMCraft.runLater(20, () -> {
                PlayerJoinEvent event = (PlayerJoinEvent) ctx.event;
                Player player = event.getPlayer();
                World world = player.getLocation().getWorld();

                if (player.getGameMode() == GameMode.SURVIVAL && worlds.containsKey(world)) {
                    updateSleepers(world);
                }
            });
        });


        /*
         * PlayerBedLeaveEvent
         */
        SMEvent.register(PlayerBedLeaveEvent.class, (ctx) -> {
            PlayerBedLeaveEvent event = (PlayerBedLeaveEvent) ctx.event;
            Player player = event.getPlayer();
            World world = player.getLocation().getWorld();

            if (player.getGameMode() == GameMode.SURVIVAL && worlds.containsKey(world)) {
                STEMCraft.runLater(1, () -> {
                    updateSleepers(world);
                });
            }
        });

        /*
         * PlayerGameModeChangeEvent
         */
        SMEvent.register(PlayerGameModeChangeEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            World world = player.getLocation().getWorld();

            if (worlds.containsKey(world)) {
                updateSleepers(world);
            }
        });

        /*
         * PlayerDeathEvent
         */
        SMEvent.register(PlayerDeathEvent.class, ctx -> {
            if (ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
                Player player = ctx.event.getEntity();
                World world = player.getLocation().getWorld();

                if (worlds.containsKey(world)) {
                    updateSleepers(world);
                }
            }
        });

        /*
         * PlayerQuitEvent
         */
        SMEvent.register(PlayerQuitEvent.class, (ctx) -> {
            updateAllSleepers();
        });

        /*
         * PlayerQuitEvent
         */
        SMEvent.register(PlayerTeleportEvent.class, (ctx) -> {
            updateAllSleepers();
        });

        return true;
    }

    /**
     * Update All Sleepers in each world
     */
    private void updateAllSleepers() {
        worlds.forEach((world, bossbar) -> {
            updateSleepers(world);
        });
    }

    /**
     * Update Sleepers in the specified world
     * 
     * @param world The world to update
     */
    private void updateSleepers(World world) {
        List<Player> players = world.getPlayers();
        int numPlayers = players.size();
        int numSleepers = 0;

        for (Player player : players) {
            if (player.isSleeping()) {
                numSleepers++;
            }
        } ;

        int required = Math.round(numPlayers * skipPercentange);
        BossBar bar = worlds.get(world);

        if (numSleepers == 0) {
            if (bar != null) {
                bar.removeAll();
                bar = null;
                worlds.put(world, null);
            }

            return;
        }

        String title = SMCommon.colorize(SMReplacer.replaceVariables(
            SMLocale.get("SKIP_NIGHT_BOSSBAR_TITLE"),
            "sleeping", String.valueOf(numSleepers), "required", String.valueOf(required)));

        if (bar == null) {
            bar = Bukkit.createBossBar(title, BarColor.BLUE, BarStyle.SOLID);
            worlds.put(world, bar);
        } else {
            bar.setTitle(title);
        }
        bar.setProgress((double) numSleepers / required);

        for (Player player : bar.getPlayers()) {
            if (player.getLocation().getWorld() != world || player.getGameMode() != GameMode.SURVIVAL) {
                bar.removePlayer(player);
            }
        }

        for (Player player : players) {
            if (!bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
        }

        if (!isSkippingNight(world)) {
            if (numSleepers >= required) {
                for (Player player : players) {
                    SMMessenger.infoLocale(player, "SKIP_NIGHT_ENOUGH_PLAYERS");
                }

                skipNight(world);
            }
        } else {
            if (numSleepers < required) {
                skipNightFinish(world);
            }
        }
    }

    /**
     * Skip the night of a specified world
     * 
     * @param world The world to skip the night
     */
    private void skipNight(World world) {
        if (!worldRandomTickCount.containsKey(world)) {
            if (world.getTime() > 13000) {
                if (!worldRandomTickCount.containsKey(world)) {
                    worldRandomTickCount.put(world, world.getGameRuleValue(GameRule.RANDOM_TICK_SPEED));
                    world.setGameRule(GameRule.RANDOM_TICK_SPEED, skipRandomTickSpeed);

                    skipNightStep(world);
                }
            }
        }
    }

    /**
     * Skip the night of a specified world
     * 
     * @param world The world to skip the night
     */
    private void skipNightStep(World world) {
        STEMCraft.runOnceDelay("skip_night_" + world.getName(), 1, () -> {
            if (world.getTime() > 1000) {
                world.setTime(world.getTime() + 100);
            }

            if (world.getTime() < 24000 && world.getTime() > 1000 && worldRandomTickCount.containsKey(world)) {
                skipNightStep(world);
            } else {
                skipNightFinish(world);
            }
        });
    }

    /**
     * Complete the skip night task
     * 
     * @param world The world to finish
     */
    private void skipNightFinish(World world) {
        if (worldRandomTickCount.containsKey(world)) {
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, worldRandomTickCount.get(world));
            worldRandomTickCount.remove(world);
        }
    }

    /**
     * Are we skipping the night in the specified world
     * 
     * @param world The world to check
     * @return boolean True if the night being skipped
     */
    public boolean isSkippingNight(World world) {
        return worldRandomTickCount.containsKey(world);
    }
}
