package com.stemcraft.feature;

import java.util.Arrays;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;

public class SMSpeed extends SMFeature {
    private String[] movementTypes = {"fly", "walk"};

    @Override
    protected Boolean onEnable() {
        // Tab Completion - Type
        SMTabComplete.register("speedtype", () -> {
            return Arrays.asList(movementTypes);
        });

        // Tab Completion - Speed
        SMTabComplete.register("speed", () -> {
            String[] speed = {"1", "1.5", "1.75", "2"};
            return Arrays.asList(speed);
        });

        new SMCommand("speed")
            .permission("stemcraft.command.speed")
            .tabComplete("{speedtype}", "{speed}", "{player}")
            .tabComplete("{speed}", "{player}")
            .action(ctx -> {
                ctx.checkArgsLocale(1, "SPEED_USAGE");

                String type = ctx.getArg(1);
                Float speed = null;
                Player targetPlayer = null;

                if (SMCommon.isInArrayIgnoreCase(movementTypes, type)) {
                    speed = ctx.getArgFloatLocale(2, "SPEED_NUMBER_USAGE");
                    targetPlayer = ctx.getArgAsPlayer(3, ctx.player);
                } else {
                    speed = ctx.getArgFloatLocale(1, "SPEED_NUMBER_USAGE");
                    targetPlayer = ctx.getArgAsPlayer(2, ctx.player);
                }

                if (ctx.fromConsole() && targetPlayer == null) {
                    ctx.returnErrorLocale("CMD_PLAYER_REQ_FROM_CONSOLE");
                }

                if (speed < 0.1f) {
                    speed = 0.1f;
                } else if (speed > 10f) {
                    speed = 10f;
                }

                if (!SMCommon.isInArrayIgnoreCase(movementTypes, type)) {
                    type = targetPlayer.isFlying() ? "fly" : "walk";
                }

                if (type.equalsIgnoreCase("fly")) {
                    targetPlayer.setFlySpeed(getRealSpeed(speed, true));
                } else {
                    targetPlayer.setWalkSpeed(getRealSpeed(speed, false));
                }

                ctx.returnInfoLocale("SPEED_CHANGED", "type", SMCommon.capitalize(type), "speed", speed.toString());
            })
            .register();

        return true;
    }

    private float getRealSpeed(final float speed, final boolean isFly) {
        final float defaultSpeed = isFly ? 0.1f : 0.2f;
        float maxSpeed = 1f;

        if (speed < 1f) {
            return defaultSpeed * speed;
        } else {
            final float ratio = ((speed - 1) / 9) * (maxSpeed - defaultSpeed);
            return ratio + defaultSpeed;
        }
    }
}
