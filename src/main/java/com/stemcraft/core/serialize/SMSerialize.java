package com.stemcraft.core.serialize;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.stemcraft.core.SMBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Utility class for serializing objects to writeable data and back.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SMSerialize {
	/**
	 * How should we de/serialize the objects in this class?
	 */
	public enum Mode {
		JSON,
		YAML
	}

    /**
     * Converts the given object into something you can safely save in file as a string
     *
     * @param object
     * @return
     */
    public static String serialize(Mode type, Object object) {
        if (object == null)
            return null;

        // Location (String)
        else if (object instanceof Location)
            return SMSerializeLocation.serialize((Location) object);

        // UUID (String)
        else if (object instanceof UUID)
            return object.toString();

        // Enum (String)
        else if (object instanceof Enum<?>)
            return object.toString();

        // World (String)
        else if (object instanceof World)
            return ((World) object).getName();

        // Entity (String)
        else if (object instanceof Entity)
            return SMBridge.getName((Entity) object);

        // PotionEffectType (String)
        else if (object instanceof PotionEffectType)
            return SMSerializePotionType.serialize((PotionEffectType) object);

        // PotionEffect (String)
        else if (object instanceof PotionEffect)
            return SMSerializePotionEffect.serialize((PotionEffect) object);

        // Enchantment (String)
        else if (object instanceof Enchantment)
            return SMSerializeEnhancement.serialize((Enchantment) object);

        throw new SerializeFailedException("Does not know how to serialize " + object.getClass().getSimpleName() + "! Does it extends ConfigSerializable? Data: " + object);
    }

    /**
     * Converts the given string into the defined object
     *
     * @param <T>
     * @param classOf
     * @param object
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T deserialize(@NonNull final Class<T> classOf, @NonNull String string) {
        
        // String
        if (classOf == String.class)
            return (T)string;

        // Integer
        else if (classOf == Integer.class)
            return (T)(Integer)Integer.parseInt(string);

        // Long
        else if (classOf == Long.class)
            return (T)Long.decode(string);

        // Double
        else if (classOf == Double.class)
            return (T)(Double)Double.parseDouble(string);

        // Float
        else if (classOf == Float.class)
            return (T)(Float)Float.parseFloat(string);

        // Boolean
        else if (classOf == Boolean.class)
            return (T)(Boolean)Boolean.parseBoolean(string);

        // Location
        else if (classOf == Location.class)
            return (T)SMSerializeLocation.deserialize(string);

        // UUID
        else if (classOf == UUID.class)
            return (T)UUID.fromString(string);

        // Enum
        else if (Enum.class.isAssignableFrom(classOf))
            return (T)SMBridge.lookupEnum((Class<Enum>) classOf, string);

        // World
        else if (classOf == World.class)
            return (T)Bukkit.getWorld(string);

        // PotionEffectType
        else if (PotionEffectType.class.isAssignableFrom(classOf))
            return (T)SMSerializePotionType.deserialize(string);

        // PotionEffect
        else if (classOf == PotionEffect.class)
            return (T)SMSerializePotionEffect.deserialize(string);

        // Enhancement
        else if (Enchantment.class.isAssignableFrom(classOf))
            return (T)SMSerializeEnhancement.deserialize(string);

        else if (classOf == Object.class) {
            // Empty
        }

        else
            throw new SerializeFailedException("Does not know how to turn " + classOf + " into a serialized object from data: " + string);

        return null;
    }

    /**
     * Thrown when cannot serialize an object because it failed to determine its type
     */
    public static class SerializeFailedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public SerializeFailedException(String reason) {
            super(reason);
        }
    }
}