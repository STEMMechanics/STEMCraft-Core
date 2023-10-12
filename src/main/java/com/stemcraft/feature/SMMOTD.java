package com.stemcraft.feature;

import org.bukkit.event.server.ServerListPingEvent;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.event.SMEvent;

public class SMMOTD extends SMFeature {
    /**
     * The MOTD title.
     */
    private static String motdTitle = "";

    /**
     * The MOTD text.
     */
    private static String motdText = "";

    /**
     * The current MOTD override string.
     */
    private static String motdTextOverride = "";

    /**
     * The current MOTD override string.
     */
    private static String motdTextVersion = "";

    /**
     * On feature enable
     */
    @Override
    protected Boolean onEnable() {
        motdTitle = SMConfig.main().getString("motd.title", "");
        motdText = SMConfig.main().getString("motd.text", "");
        if(SMConfig.main().getBoolean("motd.show-version", true)) {
            motdTextVersion = "&8v" + STEMCraft.getVersion() + " &r";
        }

        SMEvent.register(ServerListPingEvent.class, ctx -> {
            String motdString = "";

            if(motdTextOverride != "") {
                motdString = motdTextOverride;
            } else {
                motdString = motdText;
            }

            ctx.event.setMotd(SMCommon.colorize(motdTitle + "\n" + motdTextVersion + motdString));
        });

        return true;
    }

    /**
     * Get the current MOTD override
     * @return
     */
    public static String getMOTDOverride() {
        return motdTextOverride;
    }

    /**
     * Set the MOTD override
     * @param motd
     */
    public static void setMOTDOverride(String motd) {
        motdTextOverride = motd;
    }

    /**
     * Clear the MOTD override
     */
    public static void clearMOTDOverride() {
        setMOTDOverride("");
    }
}
