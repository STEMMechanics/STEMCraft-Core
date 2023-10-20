package com.stemcraft.core.serialize;

import org.bukkit.potion.PotionEffectType;
import com.stemcraft.core.SMValid;

public class SMSerializePotionType {

    /**
     * Converts a PotionEffectType into a  string
     *
     * @param effect
     * @return
     */
    public static String serialize(final PotionEffectType type) {
        return type.getName();
    }

    /**
     * Convert a string "type duration amplifier" back to PotionEffect
     *
     * @param raw
     * @return
     */
    public static PotionEffectType deserialize(final String raw) {
        if (raw == null)
            return null;

        final PotionEffectType potion = PotionEffectType.getByName(raw);

        SMValid.checkNotNull(potion, "Invalid potion '" + raw + "'! For valid names, see: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
        return potion;
    }
}
