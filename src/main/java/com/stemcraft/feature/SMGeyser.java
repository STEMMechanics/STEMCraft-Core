package com.stemcraft.feature;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.geyser.api.GeyserApi;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;

/**
 * Geyser plugin support
 */
public class SMGeyser extends SMFeature {
    private static String dependantName = "Geyser-Spigot";
    private static GeyserApi api = null;

    /**
     * Called when the feature is requested to be loaded.
     * 
     * @return If the feature loaded successfully.
     */
    @Override
    public Boolean onLoad() {
        if (!super.onLoad()) {
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin(dependantName) == null) {
            STEMCraft.warning(dependantName + " is not loaded. Features requiring this plugin won't be available");
        }

        return true;
    }

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {
        return true;
    }

    /**
     * Returns if the Geyser plugin is loaded and ready.
     * 
     * @return If the plugin is ready.
     */
    public static Boolean isGeyserReady() {
        if (api != null) {
            return true;
        }

        api = GeyserApi.api();
        return api != null;
    }

    /**
     * Test if a player is a BedRock player
     * 
     * @param player The player to test.
     * @return If the player is a geyser (or null if the dependency failed)
     */
    public static Boolean isBedrockPlayer(Player player) {
        if (!isGeyserReady()) {
            return null;
        }

        return api.isBedrockPlayer(player.getUniqueId());
    }
}
