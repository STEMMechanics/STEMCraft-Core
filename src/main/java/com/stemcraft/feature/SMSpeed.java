package com.stemcraft.feature;

import java.util.Arrays;
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
            .permission("stemcraft.speed")
            .tabComplete("{speedtype}", "{speed}", "{player}")
            .tabComplete("{speed}", "{player}")
            .action(ctx -> {
                ctx.checkArgsLocale(2, "SPEED_USAGE");

                ctx.returnError("NOT IMPLEMENTED");
            })
            .register();

        return true;
    }
}
