package com.stemcraft.core.serialize;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMValid;
import com.stemcraft.core.exception.SMInvalidWorldException;
import de.tr7zw.nbtapi.NBT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Utility class for serializing objects to writeable data and back.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SMSerialize {
    /**
     * A list of custom serializers
     */
    private static Map<Class<Object>, Function<Object, String>> serializers = new HashMap<>();

    /**
     * A list of custom deserializers
     */
    private static Map<Class<Object>, Function<String, Object>> deserializers = new HashMap<>();

    /**
     * Add a custom serializer to the list
     *
     * @param <T>
     * @param fromClass
     * @param serializer
     */
    @SuppressWarnings("unchecked")
    public static <T> void addSerializer(Class<T> fromClass, Function<Object, String> serializer) {
        serializers.put((Class<Object>) fromClass, (Function<Object, String>) serializer);
    }

    /**
     * Add a custom deserializer to the list
     *
     * @param <T>
     * @param toClass
     * @param deserializer
     */
    @SuppressWarnings("unchecked")
    public static <T> void addDeserializer(Class<T> toClass, Function<String, Object> deserializer) {
        deserializers.put((Class<Object>) toClass, (Function<String, Object>) deserializer);
    }

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
    @SuppressWarnings("deprecation")
    public static String serialize(Mode type, Object object) {
        if (object == null)
            return null;

        if (serializers.containsKey(object.getClass()))
            return serializers.get(object.getClass()).apply(object);

        else if (object instanceof Location)
            return serializeLocation((Location) object);

        else if (object instanceof UUID)
            return object.toString();

        else if (object instanceof Enum<?>)
            return object.toString();

        else if (object instanceof World)
            return ((World) object).getName();

        else if (object instanceof Entity)
            return SMBridge.getName((Entity) object);

        else if (object instanceof PotionEffectType)
            return ((PotionEffectType) object).getName();

        else if (object instanceof PotionEffect)
            return serializePotionEffect((PotionEffect) object);

        else if (object instanceof Enchantment)
            return ((Enchantment) object).getName();

        else if (object instanceof ItemStack)
            return serializeItemStack((ItemStack) object);

        else if (object instanceof ItemStack[])
            return serializeItemStackArray((ItemStack[]) object);

        else if (object instanceof ConfigurationSerializable)
            return serializeCS((ConfigurationSerializable) object);

        else if (object instanceof BaseComponent)
            return serializeJSON((BaseComponent) object);

        else if (object instanceof BaseComponent[])
            return serializeJSON((BaseComponent[]) object);

        else if (object instanceof Iterable || object.getClass().isArray())
            return serializeJSON(object);

        else if (object instanceof Map)
            return serializeJSON(object);

        throw new SerializeFailedException("Does not know how to serialize " + object.getClass().getSimpleName() + "! Does it extends ConfigSerializable? Data: " + object);
    }

    /**
     * Converts a {@link Location} into "world x y z yaw pitch" string
     * Decimals not supported, use {@link #deserializeLocationD(Object)} for them
     *
     * @param loc
     * @return
     */
    public static String serializeLocation(final Location loc) {
        return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + (loc.getPitch() != 0F || loc.getYaw() != 0F ? " " + Math.round(loc.getYaw()) + " " + Math.round(loc.getPitch()) : "");
    }

    /**
     * Converts a {@link PotionEffect} into a "type duration amplifier" string
     *
     * @param effect
     * @return
     */
    private static String serializePotionEffect(final PotionEffect effect) {
        return effect.getType().getName() + " " + effect.getDuration() + " " + effect.getAmplifier();
    }

    private static String serializeItemStack(final ItemStack item) {
        Map<String, Object> map = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        
        map = item.serialize();
        if(meta != null) {
            map.put("meta", meta.serialize());
        }

        return serializeJSON(map);
    }

    private static String serializeItemStackArray(final ItemStack[] item) {
        List<Map<String, Object>> list = new ArrayList<>();

        for(int i = 0; i < item.length; i++) {
            if(item[i] != null && item[i].getType() != Material.AIR) {
                Map<String, Object> map = new HashMap<>();
                String serializedItem = serializeItemStack(item[i]);

                map.put("slot", i);
                map.put("item", serializedItem);
                list.add(map);
            }
        }

        return serializeJSON(list);
    }

    private static String serializeCS(final ConfigurationSerializable cs) {
        Map<String, Object> map = cs.serialize();
        return serializeJSON(map);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Converting stored strings from your files back into classes
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Please see {@link #deserialize(Class, Object)}, plus that this method
     * allows you to parse through more arguments to the static deserialize method
     *
     * @param <T>
     * @param classOf
     * @param object
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
    public static <T> T deserialize(@NonNull final Class<T> classOf, @NonNull String string) {
        if (deserializers.containsKey(classOf))
            return (T)deserializers.get(classOf).apply(string);

        if (classOf == String.class)
            return (T)string;

        else if (classOf == Integer.class)
            return (T)(Integer)Integer.parseInt(string);

        else if (classOf == Long.class)
            return (T)Long.decode(string);

        else if (classOf == Double.class)
            return (T)(Double)Double.parseDouble(string);

        else if (classOf == Float.class)
            return (T)(Float)Float.parseFloat(string);

        else if (classOf == Boolean.class)
            return (T)(Boolean)Boolean.parseBoolean(string);

        else if (classOf == Location.class)
            return (T)deserializeLocation(string);

        else if (classOf == UUID.class)
            return (T)UUID.fromString(string);

        else if (Enum.class.isAssignableFrom(classOf))
            return (T)SMBridge.lookupEnum((Class<Enum>) classOf, string);

        else if (classOf == World.class)
            return (T)Bukkit.getWorld(string);
        
        // else if (classOf == Entity.class)
        //     return (T)Bukkit.

        //     else if (classOf == PotionEffectType.class)
        //     object = PotionEffectType.getByName(object.toString());

        // else if (classOf == PotionEffect.class)
        //     object = deserializePotionEffect(object);

        // else if (classOf == ItemStack.class)
        //     object = deserializeItemStack(object.toString());

        // else if (classOf == ItemStack[].class)
        //     object = deserializeItemStackArray(object.toString());


        // else if (classOf == BaseComponent.class) {
        //     final BaseComponent[] deserialized = ComponentSerializer.parse(object.toString());
        //     SMValid.checkBoolean(deserialized.length == 1, "Failed to deserialize into singular BaseComponent: " + object);

        //     object = deserialized[0];

        // } else if (classOf == BaseComponent[].class)
        //     object = ComponentSerializer.parse(object.toString());

        // else if (Enchantment.class.isAssignableFrom(classOf)) {
        //     String name = object.toString().toLowerCase();
        //     Enchantment enchant = Enchantment.getByName(name);

        //     if (enchant == null) {
        //         name = name.toUpperCase();

        //         enchant = Enchantment.getByName(name);
        //     }

        //     if (enchant == null) {
        //         name = EnchantmentWrapper.toBukkit(name);
        //         enchant = Enchantment.getByName(name);

        //         if (enchant == null)
        //             enchant = Enchantment.getByName(name.toLowerCase());

        //         if (enchant == null)
        //             enchant = Enchantment.getByName(name.toUpperCase());
        //     }

        //     SMValid.checkNotNull(enchant, "Invalid enchantment '" + name + "'! For SMValid names, see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");
        //     object = enchant;
        // }

        // else if (PotionEffectType.class.isAssignableFrom(classOf)) {
        //     final String name = PotionWrapper.getBukkitName(object.toString());
        //     final PotionEffectType potion = PotionEffectType.getByName(name);

        //     SMValid.checkNotNull(potion, "Invalid potion '" + name + "'! For SMValid names, see: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
        //     object = potion;
        // }


        // else if (List.class.isAssignableFrom(classOf) && object instanceof List) {
        //     // Good

        // } else if (Map.class.isAssignableFrom(classOf)) {
        //     // if (object instanceof Map) {
        //         // return (T) object;
        //         // return (T) deserializeMap(object.toString());
        //     // }

        //     // throw new SerializeFailedException("Does not know how to turn " + object.getClass().getSimpleName() + " into a Map! (Keep in mind we can only serialize into Map<String, Object> Data: " + object);

        // } else if (classOf.isArray()) {
        //     final Class<?> arrayType = classOf.getComponentType();
        //     T[] array;

        //     if (object instanceof List) {
        //         final List<?> rawList = (List<?>) object;
        //         array = (T[]) Array.newInstance(classOf.getComponentType(), rawList.size());

        //         for (int i = 0; i < rawList.size(); i++) {
        //             final Object element = rawList.get(i);

        //             array[i] = element == null ? null : (T) deserialize(arrayType, element);
        //         }
        //     }

        //     else {
        //         final Object[] rawArray = (Object[]) object;
        //         array = (T[]) Array.newInstance(classOf.getComponentType(), rawArray.length);

        //         for (int i = 0; i < array.length; i++)
        //             array[i] = rawArray[i] == null ? null : (T) deserialize(classOf.getComponentType(), rawArray[i]);
        //     }

            // return (T) array;

        // }

        // else if (classOf == Object.class) {
        //     // Good
        // }

        // else
        //     throw new SerializeFailedException("Does not know how to turn " + classOf + " into a serialized object from data: " + object);

        return (T) null;
    }

    /**
     * Converts a string into location, see {@link #deserializeLocation(Object)} for how strings are saved
     * Decimals not supported, use {@link #deserializeLocationD(Object)} to use them
     *
     * @param raw
     * @return
     */
    public static Location deserializeLocation(Object raw) {
        if (raw == null)
            return null;

        if (raw instanceof Location)
            return (Location) raw;

        raw = raw.toString().replace("\"", "");

        final String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
        SMValid.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

        final String world = parts[0];
        final World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null)
            throw new SMInvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);

        final int x = Integer.parseInt(parts[1]), y = Integer.parseInt(parts[2]), z = Integer.parseInt(parts[3]);
        final float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0"), pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");

        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }

    /**
     * Converts a string into a location with decimal support
     * Unused but you can use this for your own parser storing exact decimals
     *
     * @param raw
     * @return
     */
    public static Location deserializeLocationD(Object raw) {
        if (raw == null)
            return null;

        if (raw instanceof Location)
            return (Location) raw;

        raw = raw.toString().replace("\"", "");

        final String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
        SMValid.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

        final String world = parts[0];
        final World bukkitWorld = Bukkit.getWorld(world);

        if (bukkitWorld == null)
            throw new SMInvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);

        final double x = Double.parseDouble(parts[1]), y = Double.parseDouble(parts[2]), z = Double.parseDouble(parts[3]);
        final float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0"), pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");

        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }

    /**
     * Convert a raw object back to {@link PotionEffect}
     *
     * @param raw
     * @return
     */
    private static PotionEffect deserializePotionEffect(final Object raw) {
        if (raw == null)
            return null;

        if (raw instanceof PotionEffect)
            return (PotionEffect) raw;

        final String[] parts = raw.toString().split(" ");
        SMValid.checkBoolean(parts.length == 3, "Expected PotionEffect (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

        final String typeRaw = parts[0];
        final PotionEffectType type = PotionEffectType.getByName(typeRaw);

        final int duration = Integer.parseInt(parts[1]);
        final int amplifier = Integer.parseInt(parts[2]);

        return new PotionEffect(type, duration, amplifier);
    }

    private static ItemStack deserializeItemStack(final String item) {
        return NBT.itemStackFromNBT(NBT.parseNBT(item));
    }

    private static ItemStack[] deserializeItemStackArray(final String items) {
        return NBT.itemStackArrayFromNBT(NBT.parseNBT(items));
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

    /**
     * A simple class holding some of the potion names
     */
    @RequiredArgsConstructor
    protected enum PotionWrapper {

        SLOW("SLOW", "Slowness"),
        STRENGTH("INCREASE_DAMAGE"),
        JUMP_BOOST("JUMP"),
        INSTANT_HEAL("INSTANT_HEALTH"),
        REGEN("REGENERATION");

        private final String bukkitName;
        private final String minecraftName;

        PotionWrapper(String bukkitName) {
            this(bukkitName, null);
        }

        protected static String getLocalizedName(String name) {
            String localizedName = name;

            for (final PotionWrapper e : values())
                if (name.toUpperCase().replace(" ", "_").equals(e.bukkitName)) {
                    localizedName = e.getMinecraftName();

                    break;
                }

            return SMCommon.capitalize(localizedName.replace("_", " "));
        }

        protected static String getBukkitName(String name) {
            name = name.toUpperCase().replace(" ", "_");

            for (final PotionWrapper wrapper : values())
                if (wrapper.toString().equalsIgnoreCase(name) || wrapper.minecraftName != null && wrapper.minecraftName.equalsIgnoreCase(name))
                    return wrapper.bukkitName;

            return name;
        }

        public String getMinecraftName() {
            return SMCommon.getOrDefault(this.minecraftName, this.bukkitName);
        }
    }

    /**
     * A simple class holding some of the enchantments names
     */
    @RequiredArgsConstructor
    protected enum EnchantmentWrapper {
        PROTECTION("PROTECTION_ENVIRONMENTAL"),
        FIRE_PROTECTION("PROTECTION_FIRE"),
        FEATHER_FALLING("PROTECTION_FALL"),
        BLAST_PROTECTION("PROTECTION_EXPLOSIONS"),
        PROJECTILE_PROTECTION("PROTECTION_PROJECTILE"),
        RESPIRATION("OXYGEN"),
        AQUA_AFFINITY("WATER_WORKER"),
        THORN("THORNS"),
        CURSE_OF_VANISHING("VANISHING_CURSE"),
        CURSE_OF_BINDING("BINDING_CURSE"),
        SHARPNESS("DAMAGE_ALL"),
        SMITE("DAMAGE_UNDEAD"),
        BANE_OF_ARTHROPODS("DAMAGE_ARTHROPODS"),
        LOOTING("LOOT_BONUS_MOBS"),
        SWEEPING_EDGE("SWEEPING"),
        EFFICIENCY("DIG_SPEED"),
        UNBREAKING("DURABILITY"),
        FORTUNE("LOOT_BONUS_BLOCKS"),
        POWER("ARROW_DAMAGE"),
        PUNCH("ARROW_KNOCKBACK"),
        FLAME("ARROW_FIRE"),
        INFINITY("ARROW_INFINITE"),
        LUCK_OF_THE_SEA("LUCK");

        private final String bukkitName;

        protected static String toBukkit(String name) {
            name = name.toUpperCase().replace(" ", "_");

            for (final EnchantmentWrapper e : values())
                if (e.toString().equals(name))
                    return e.bukkitName;

            return name;
        }

        protected static String toMinecraft(String name) {
            name = name.toUpperCase().replace(" ", "_");

            for (final EnchantmentWrapper e : values())
                if (name.equals(e.bukkitName))
                    return SMCommon.beautifyCapitalize(e);

            return SMCommon.capitalize(name);
        }

        public String getBukkitName() {
            return this.bukkitName != null ? this.bukkitName : this.name();
        }
    }

    /**
     * Is item a Java primative
     * @param item primative to test
     * @return if item is a primative type
     */
    private static boolean isPrimitive(Object item) {
        return item instanceof String ||
                item instanceof Byte ||
                item instanceof Short ||
                item instanceof Integer ||
                item instanceof Long ||
                item instanceof Float ||
                item instanceof Double ||
                item instanceof Boolean ||
                item instanceof Character;
    }

    /**
     * Is item supported by the builder
     * @param item The item to test
     * @param multiDimensional Support multi-dimensional items
     * @return If the item is supported
     */
    private static boolean isSupportedType(Object item, Boolean multiDimensional, Boolean throwError) {
        if (item instanceof List) {
            List<?> list = (List<?>) item;
            for (Object element : list) {
                if(isPrimitive(element))
                    continue;
                
                boolean isSupportedContainer = element instanceof List || element instanceof Map || element.getClass().isArray();

                if (isSupportedContainer && multiDimensional) {
                    if (!isSupportedType(element, true, throwError)) {
                        return false;
                    }
                } else {
                    if (throwError) {
                        throw new SerializeFailedException(element.getClass().getSimpleName() + " is not supported by the serialize builder");
                    }
                    return false;
                }
            }
            return true;
        }

        if (item instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) item;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    return false;
                }

                if (!isPrimitive(entry.getValue()) && !multiDimensional) {
                    return false;
                }

                if(!isSupportedType(entry.getValue(), true, throwError)) {
                    return false;
                }

            }
            return true;
        }

        if (item.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(item);
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(item, i);
                if (!isPrimitive(element) && !multiDimensional) {
                    return false;
                }
    
                if (!isSupportedType(element, true, throwError)) {
                    return false;
                }
            }
            return true;
        }
    
        return isPrimitive(item);
    }

    /**
     * Convert object to YAML
     * @param item Object to convert
     * @return converted YAML string
     */
    private static String toYAML(final Object item) {
        YamlConfiguration config = new YamlConfiguration();

        if (item.getClass().isArray()) {
            Object[] items = (Object[]) item;
            List<Object> serializedItems = new ArrayList<>();
            for (Object subItem : items) {
                serializedItems.add(subItem == null ? "" : serializeJSON(subItem));
            }
            config.set("items", serializedItems);  // Set to root
        } else if (item instanceof List) {
            List<Object> items = (List<Object>) item;
            List<Object> serializedItems = new ArrayList<>();
            for (Object subItem : items) {
                serializedItems.add(subItem == null ? "" : serializeJSON(subItem));
            }
            config.set("items", serializedItems);  // Set to root
        } else if (item instanceof ConfigurationSerializable) {
            Map<String, Object> serializedMap = ((ConfigurationSerializable) item).serialize();
            for (Map.Entry<String, Object> entry : serializedMap.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
        } else if (item instanceof Map) {
            Map<String, Object> itemMap = (Map<String, Object>) item;
            for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
        } else {
            throw new SerializeFailedException("Cannot serialize " + item.getClass());
        }

        return config.saveToString();
    }

    /**
     * Convert Object to JSON
     * @param item item to convert
     * @return converted string
     */
    private static String toJSON(final Object item) {
        ObjectMapper objectMapper = new ObjectMapper();

        if(!isSupportedType(item, true)) {

        }

        try {
            return objectMapper.writeValueAsString(item);
        } catch(Exception e) {
            throw new SerializeFailedException(e.getLocalizedMessage());
        }
    }
}