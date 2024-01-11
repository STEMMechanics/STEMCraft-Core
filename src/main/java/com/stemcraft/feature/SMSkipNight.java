package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.stemcraft.core.event.SMEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
    private List<String> worlds = new ArrayList<>();
    private List<UUID> sleepDelay = new ArrayList<>();
    private List<UUID> playersSleeping = new ArrayList<>();
    private BossBar sleepingBossBar;
    private List<World> skippingNight = new ArrayList<>();

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        skipPercentange = SMConfig.main().getFloat("skip-night.required", 1f);
        worlds = SMConfig.main().getStringList("skip-night.worlds");

        SMEvent.register(PlayerBedEnterEvent.class, (ctx) -> {
            PlayerBedEnterEvent event = (PlayerBedEnterEvent) ctx.event;
            Player player = event.getPlayer();

            if (player.getGameMode() == GameMode.SURVIVAL
                &&
                SMCommon.isInArrayIgnoreCase(worlds, player.getLocation().getWorld().getName())) {
                if (sleepDelay.contains(player.getUniqueId())) {
                    SMMessenger.infoLocale(player, "SKIP_NIGHT_NEED_TO_WAIT_TO_SLEEP");
                    event.setCancelled(true);
                } else {
                    addPlayerSleeping(player);
                }
            }
        });

        SMEvent.register(PlayerBedLeaveEvent.class, (ctx) -> {
            PlayerBedLeaveEvent event = (PlayerBedLeaveEvent) ctx.event;
            Player player = event.getPlayer();

            removePlayerSleeping(player);

            if (player.getGameMode() == GameMode.SURVIVAL) {
                if (!sleepDelay.contains(player.getUniqueId())) {
                    sleepDelay.add(player.getUniqueId());
                    STEMCraft.runOnce("sleep_" + player.getUniqueId().toString(), 900, () -> {
                        sleepDelay.remove(player.getUniqueId());
                    });
                }
            }
        });

        SMEvent.register(PlayerQuitEvent.class, (ctx) -> {
            PlayerQuitEvent event = (PlayerQuitEvent) ctx.event;
            Player player = event.getPlayer();

            removePlayerSleeping(player);
        });

        SMEvent.register(PlayerTeleportEvent.class, (ctx) -> {
            PlayerTeleportEvent event = (PlayerTeleportEvent) ctx.event;
            Player player = event.getPlayer();

            removePlayerSleeping(player);
        });

        return true;
    }

    private void addPlayerSleeping(Player player) {
        if (!playersSleeping.contains(player.getUniqueId())) {
            playersSleeping.add(player.getUniqueId());
        }

        if (sleepingBossBar == null) {
            String title =
                SMReplacer.replaceVariables(SMLocale.get("SKIP_NIGHT_BOSSBAR_TITLE"), "sleeping", "-", "required", "-");

            sleepingBossBar = Bukkit.createBossBar(title, BarColor.BLUE, BarStyle.SOLID);
        }

        updateSleepers();
    }

    private void removePlayerSleeping(Player player) {
        playersSleeping.remove(player.getUniqueId());

        if (sleepingBossBar != null) {
            if (sleepingBossBar.getPlayers().contains(player)) {
                sleepingBossBar.removePlayer(player);
            }

            if (sleepingBossBar.getPlayers().size() == 0) {
                sleepingBossBar.removeAll();
                sleepingBossBar = null;
            }
        }
    }

    private void updateSleepers() {
        if (sleepingBossBar == null || playersSleeping.size() == 0) {
            return;
        }

        List<World> sleepWorlds = new ArrayList<>();
        List<Player> sleepPlayers = new ArrayList<>();

        for (String worldName : worlds) {
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                sleepWorlds.add(world);
                sleepPlayers.addAll(world.getPlayers());
            }
        }

        int sleepers = playersSleeping.size();
        int required = Math.round(sleepPlayers.size() * skipPercentange);

        String title = SMReplacer.replaceVariables(SMLocale.get("SKIP_NIGHT_BOSSBAR_TITLE"), "sleeping",
            String.valueOf(sleepers), "required", String.valueOf(required));
        sleepingBossBar.setTitle(title);
        sleepingBossBar.setProgress(sleepers / required);

        if (!isSkippingNight()) {
            if (sleepers >= required) {
                for (Player player : sleepPlayers) {
                    SMMessenger.infoLocale(player, "SKIP_NIGHT_ENOUGH_PLAYERS");
                }

                for (World world : sleepWorlds) {
                    skipNight(world);
                }
            }
        }
    }

    public void skipNight(World world) {
        if (world.getTime() > 13000) {
            if (!skippingNight.contains(world)) {
                skippingNight.add(world);
            }

            STEMCraft.runOnceDelay("skip_night_" + world.getName(), 1, () -> {
                if (world.getTime() > 1000) {
                    world.setTime(world.getTime() + 100);
                }

                if (world.getTime() < 24000 && world.getTime() > 1000) {
                    skipNight(world);
                } else {
                    skippingNight.remove(world);
                }
            });
        } else {
            skippingNight.remove(world);
        }
    }

    public boolean isSkippingNight(World world) {
        return skippingNight.contains(world);
    }

    public boolean isSkippingNight() {
        return skippingNight.size() > 0;
    }

}
