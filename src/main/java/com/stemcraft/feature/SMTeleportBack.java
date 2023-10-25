package com.stemcraft.feature;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.event.SMEvent;

public class SMTeleportBack extends SMFeature {
    private final HashMap<UUID, Location> playerPreviousLocations = new HashMap<>();

    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerTeleportEvent.class, ctx -> {
            this.onPlayerTeleport(ctx.event);
        });

        SMEvent.register(PlayerDeathEvent.class, ctx -> {
            if(ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
                Player player = ctx.event.getEntity();
                this.playerPreviousLocations.put(player.getUniqueId(), player.getLocation());
            }
        });

        new SMCommand("back")
            .permission("stemcraft.teleport.back")
            .action(ctx -> {
                ctx.checkNotConsole();
                UUID playerId = ctx.player.getUniqueId();

                if (!this.playerPreviousLocations.containsKey(playerId)) {
                    ctx.returnErrorLocale("NO_BACK_LOCATION", "player", ctx.senderName());
                }

                Location previousLocation = this.playerPreviousLocations.get(playerId);
                SMCommon.delayedPlayerTeleport(ctx.player, previousLocation);
            })
            .register();
    
        return true;
    }

    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        this.playerPreviousLocations.put(player.getUniqueId(), event.getFrom());
    }
}
