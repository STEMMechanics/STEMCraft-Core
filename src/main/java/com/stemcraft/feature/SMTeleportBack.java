package com.stemcraft.feature;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SMTeleportBack extends SMFeature {
    private final HashMap<UUID, Location> playerPreviousLocations = new HashMap<>();

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("NO_BACK_LOCATION", "There is no location to teleport back to %PLAYER_NAME%");

        this.plugin.getEventManager().registerEvent(PlayerTeleportEvent.class, (listener, event) -> {
            this.onPlayerTeleport((PlayerTeleportEvent)event);
        });

        this.plugin.getCommandManager().registerCommand("back", (sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID playerId = player.getUniqueId();
                
                if (!this.playerPreviousLocations.containsKey(playerId)) {
                    HashMap<String, String> replacements = new HashMap<>();
                    
                    replacements.put("PLAYER_NAME", player.getName());

                    player.sendMessage(this.plugin.getLanguageManager().getPhrase("NO_BACK_LOCATION", replacements));
                    return true;
                }

                Location previousLocation = this.playerPreviousLocations.get(playerId);
                player.teleport(previousLocation);
                return true;
            }

            this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
            return true;
        });
    
        return true;
    }

    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        this.playerPreviousLocations.put(player.getUniqueId(), event.getFrom());
    }
}
