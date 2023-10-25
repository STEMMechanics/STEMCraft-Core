package com.stemcraft.feature;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;

public class SMSeen extends SMFeature {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
    private String permission = "stemcraft.seen";

    @Override
    protected Boolean onEnable() {
        new SMCommand("seen")
            .tabComplete("{offline-player}")
            .permission(permission)
            .action(ctx -> {
                ctx.checkArgs(1, "SEEN_USAGE");
                Player targetPlayer = ctx.getArgAsPlayer(1, null);
                
                if(targetPlayer == null) {
                    ctx.returnErrorLocale("CMD_PLAYER_NOT_FOUND");
                    return;
                }
                
                Date lastLogin = new Date(targetPlayer.getLastPlayed());
                ctx.returnInfoLocale("SEEN_RESULT", "player", targetPlayer.getName(), "last-login", dateFormat.format(lastLogin));
            })
            .register();

        return true;
    }
}
