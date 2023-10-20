package com.stemcraft.core.serialize;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.stemcraft.core.SMValid;

public class SMSerializePotionEffect {

    /**
     * Converts a PotionEffect into a  string
     *
     * @param effect
     * @return
     */
    public static String serialize(final PotionEffect effect) {
        return effect.getType().getName() + " " + effect.getDuration() + " " + effect.getAmplifier();
    }


    /**
     * Convert a string "type duration amplifier" back to PotionEffect
     *
     * @param raw
     * @return
     */
    public static PotionEffect deserialize(final String raw) {
        if (raw == null)
            return null;

        final String[] parts = raw.toString().split(" ");
        SMValid.checkBoolean(parts.length == 3, "Expected PotionEffect (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

        final String typeRaw = parts[0];
        final PotionEffectType type = PotionEffectType.getByName(typeRaw);

        final int duration = Integer.parseInt(parts[1]);
        final int amplifier = Integer.parseInt(parts[2]);

        return new PotionEffect(type, duration, amplifier);
    }
}
