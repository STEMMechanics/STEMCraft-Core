package com.stemcraft.core.serialize;

import org.bukkit.enchantments.Enchantment;
import com.stemcraft.core.SMValid;

public class SMSerializeEnhancement {

    /**
     * Converts a Enhancement into a string
     *
     * @param effect
     * @return
     */
    public static String serialize(final Enchantment enhancement) {
        return enhancement.getName();
    }

    /**
     * Converts a String into an Enhancement
     *
     * @param raw
     * @return
     */
    public static Enchantment deserialize(final String raw) {
        if (raw == null)
            return null;

        String name = raw.toLowerCase();
        Enchantment enchant = Enchantment.getByName(name);

        if (enchant == null) {
            name = name.toUpperCase();

            enchant = Enchantment.getByName(name);
        }

        SMValid.checkNotNull(enchant, "Invalid enchantment '" + name + "'! For valid names, see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");
        return enchant;
    }
}
