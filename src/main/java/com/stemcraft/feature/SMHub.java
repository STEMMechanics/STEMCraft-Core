package com.stemcraft.feature;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class SMHub extends SMFeature {
    private static final boolean DIMENSIONAL_DEBUG = false;

    HashMap<String, String[]> additionalCommandsForDimensionPrefixes = new HashMap<>();

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("HUB_TELEPORTED", "&eTeleported back to the hub");

        String[] aliases = new String[]{};

        // Hub Command
        this.plugin.getCommandManager().registerCommand("hub", (sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                return true;
            }

            if (!sender.hasPermission("stemcraft.hub")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            Player player = (Player)sender;

            World playerWorld = player.getWorld();
            String playerDimension = player.getWorld().getName();

            if (DIMENSIONAL_DEBUG) {
                this.plugin.getLogger().info("Player: " + player.getName());
                this.plugin.getLogger().info("World Name: " + playerWorld.getName());
                this.plugin.getLogger().info("World UID: " + playerWorld.getUID());
            }

            for (String dimensionPrefix: additionalCommandsForDimensionPrefixes.keySet()) {
                if (DIMENSIONAL_DEBUG) {
                    this.plugin.getLogger().info("Checking dimension prefix '" + dimensionPrefix + "'.");
                }

                if (playerDimension.startsWith(dimensionPrefix)) {
                    if (DIMENSIONAL_DEBUG) {
                        this.plugin.getLogger().info("Dimension prefix is a match.");
                        this.plugin.getLogger().info("Running commands for dimension prefix.");
                    }

                    for (String commandToRun : additionalCommandsForDimensionPrefixes.get(dimensionPrefix)) {
                        if (DIMENSIONAL_DEBUG) this.plugin.getLogger().info("Running command '/" + commandToRun + "'.");

                        boolean commandSuccess = player.performCommand(commandToRun);

                        if (!commandSuccess) {
                            this.plugin.getLogger().warning("An error occurred while running the dimensional hub command '" + commandToRun + "'. Please report this to the developers of STEMCraft.");
                        }
                    }

                    if (DIMENSIONAL_DEBUG) this.plugin.getLogger().info("Finished running commands for dimension prefix.");
                } else {
                    if (DIMENSIONAL_DEBUG) this.plugin.getLogger().info("Dimension prefix is not a match.");
                }
            }

            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());

            return true;
        }, aliases);

        // Player Join Event
        this.plugin.getEventManager().registerEvent(PlayerJoinEvent.class, (listener, rawEvent) -> {
            PlayerJoinEvent event = (PlayerJoinEvent)rawEvent;
            Player player = event.getPlayer();

            if(!player.hasPermission("stemcraft.hub.override")) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        });

        return true;
    }
}
