package com.stemcraft.core.serialize;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiFunction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMValid;
import com.stemcraft.core.exception.SMInvalidWorldException;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
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
    private static Map<Class<Object>, BiFunction<Boolean, Object, Object>> serializers = new HashMap<>();

    /**
     * Add a custom serializer to the list
     *
     * @param <T>
     * @param fromClass
     * @param serializer
     */
    @SuppressWarnings("unchecked")
    public static <T> void addSerializer(Class<T> fromClass, BiFunction<Boolean, Object, Object> serializer) {
        serializers.put((Class<Object>) fromClass, (BiFunction<Boolean, Object, Object>) serializer);
    }

    public static void initalize() {
        SMSerialize.addSerializer(World.class, (serialize, data) -> {
            if(serialize) {
                return ((World)data).getName();
            } else {
                return Bukkit.getServer().getWorld(((String)data));
            }
        });
    }

    /**
     * Converts the given object into something you can safely save in file as a string
     *
     * @param mode determines the file that the object originated from, if unsure just set to YAML
     * @param object
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String serialize(Object object) {
        if (object == null)
            return null;

        // if (serializers.containsKey(object.getClass()))
        //     return serializers.get(object.getClass()).apply(object);

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
            return serializeItemStack((ItemStack)object);

        else if (object instanceof ItemStack[])
            return serializeItemStackArray((ItemStack[])object);

        // else if (object instanceof BaseComponent)
        //     return SMJSON.toJSON((BaseComponent) object);

        // else if (object instanceof BaseComponent[]) {
        //     return SMJSON.toJSON((BaseComponent[]) object);

        // } else if (object instanceof Iterable || object.getClass().isArray()) {
        //     return SMJSON.toJSON(object);

        else if (object instanceof Map)
            return serializeMap((Map<Object, Object>)object);

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
        return NBT.itemStackToNBT(item).toString();
    }

    private static String serializeItemStackArray(final ItemStack[] item) {
        return NBT.itemStackArrayToNBT(item).toString();
    }

    private static String serializeMap(final Map<Object, Object> map) {
        ReadWriteNBT nbt = NBT.createNBTObject();

        for (Entry<Object, Object> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("Only string keys are permitted to be serialized");
            }
            
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if(value instanceof Boolean)
                nbt.setBoolean(key, (Boolean)value);
            else if(value instanceof Byte)
                nbt.setByte(key, (Byte)value);
            else if(value instanceof byte[])
                nbt.setByteArray(key, (byte[])value);
            else if(value instanceof Double)
                nbt.setDouble(key, (Double)value);
            else if(value instanceof Float)
                nbt.setFloat(key, (Float)value);
            else if(value instanceof Integer)
                nbt.setInteger(key, (Integer)value);
            else if(value instanceof int[])
                nbt.setIntArray(key, (int[])value);
            else if(value instanceof Long)
                nbt.setLong(key, (Long)value);
            else if(value instanceof Short)
                nbt.setShort(key, (Short)value);
            else if(value instanceof String)
                nbt.setString(key, (String)value);
            else if(value instanceof UUID)
                nbt.setUUID(key, (UUID)value);
            else if(value instanceof ItemStack)
                nbt.setItemStack(key, (ItemStack)value);
            else if(value instanceof ItemStack[])
                nbt.setItemStackArray(key, (ItemStack[])value);
            else
                nbt.setString(key, serialize(value));
        }

        return nbt.toString();
    }
    
    private static Map<String, Object> deserializeMap(final String mapString) {
        ReadWriteNBT nbt = NBT.parseNBT(mapString);
        Map<String, Object> map = new HashMap<>();

        for (String key : nbt.getKeys()) {

            NBTType type = nbt.getType(key);

            if(type == NBTType.NBTTagByte)
                map.put(key, nbt.getByte(key));
            else if(type == NBTType.NBTTagByteArray)
                map.put(key, nbt.getByteArray(key));
            else if(type == NBTType.NBTTagCompound)
                map.put(key, nbt.getCompound(key));
            else if(type == NBTType.NBTTagDouble)
                map.put(key, nbt.getDouble(key));
            else if(type == NBTType.NBTTagFloat)
                map.put(key, nbt.getFloat(key));
            else if(type == NBTType.NBTTagInt)
                map.put(key, nbt.getInteger(key));
            else if(type == NBTType.NBTTagIntArray)
                map.put(key, nbt.getIntArray(key));
            else if(type == NBTType.NBTTagLong)
                map.put(key, nbt.getLong(key));
            else if(type == NBTType.NBTTagShort)
                map.put(key, nbt.getShort(key));
            else if(type == NBTType.NBTTagString)
                map.put(key, nbt.getString(key));
            else if(type == NBTType.NBTTagList) {
                NBTType listType = nbt.getListType(key);

                if(listType == NBTType.NBTTagCompound)
                    map.put(key, nbt.getCompoundList(key));
                else if(listType == NBTType.NBTTagDouble)
                    map.put(key, nbt.getDoubleList(key));
                else if(listType == NBTType.NBTTagFloat)
                    map.put(key, nbt.getFloatList(key));
                else if(listType == NBTType.NBTTagIntArray)
                    map.put(key, nbt.getIntArrayList(key));
                else if(listType == NBTType.NBTTagInt)
                    map.put(key, nbt.getIntegerList(key));
                else if(listType == NBTType.NBTTagLong)
                    map.put(key, nbt.getLongList(key));
                else if(listType == NBTType.NBTTagString)
                    map.put(key, nbt.getStringList(key));
            }
        }

        return map;
    }
    // ------------------------------------------------------------------------------------------------------------
    // Converting stored strings from your files back into classes
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Please see {@link #deserialize(Class, Object)}, plus that this method
     * allows you to parse through more arguments to the static deserialize method
     *
     * @param <T>
     * @param mode determines the file that the object originated from, if unsure just set to YAML
     * @param classOf
     * @param object
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
    public static <T> T deserialize(@NonNull final Class<T> classOf, @NonNull Object object) {

        if (classOf == String.class)
            object = object.toString();

        else if (classOf == Integer.class)
            object = Integer.parseInt(object.toString());

        else if (classOf == Long.class)
            object = Long.decode(object.toString());

        else if (classOf == Double.class)
            object = Double.parseDouble(object.toString());

        else if (classOf == Float.class)
            object = Float.parseFloat(object.toString());

        else if (classOf == Boolean.class)
            object = Boolean.parseBoolean(object.toString());

        else if (classOf == Location.class)
            object = deserializeLocation(object);

        else if (classOf == PotionEffectType.class)
            object = PotionEffectType.getByName(object.toString());

        else if (classOf == PotionEffect.class)
            object = deserializePotionEffect(object);

        else if (classOf == ItemStack.class)
            object = deserializeItemStack(object.toString());

        else if (classOf == ItemStack[].class)
            object = deserializeItemStackArray(object.toString());

            else if (classOf == UUID.class)
            object = UUID.fromString(object.toString());

        else if (classOf == BaseComponent.class) {
            final BaseComponent[] deserialized = ComponentSerializer.parse(object.toString());
            SMValid.checkBoolean(deserialized.length == 1, "Failed to deserialize into singular BaseComponent: " + object);

            object = deserialized[0];

        } else if (classOf == BaseComponent[].class)
            object = ComponentSerializer.parse(object.toString());

        else if (Enchantment.class.isAssignableFrom(classOf)) {
            String name = object.toString().toLowerCase();
            Enchantment enchant = Enchantment.getByName(name);

            if (enchant == null) {
                name = name.toUpperCase();

                enchant = Enchantment.getByName(name);
            }

            if (enchant == null) {
                name = EnchantmentWrapper.toBukkit(name);
                enchant = Enchantment.getByName(name);

                if (enchant == null)
                    enchant = Enchantment.getByName(name.toLowerCase());

                if (enchant == null)
                    enchant = Enchantment.getByName(name.toUpperCase());
            }

            SMValid.checkNotNull(enchant, "Invalid enchantment '" + name + "'! For SMValid names, see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");
            object = enchant;
        }

        else if (PotionEffectType.class.isAssignableFrom(classOf)) {
            final String name = PotionWrapper.getBukkitName(object.toString());
            final PotionEffectType potion = PotionEffectType.getByName(name);

            SMValid.checkNotNull(potion, "Invalid potion '" + name + "'! For SMValid names, see: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
            object = potion;
        }

        else if (Enum.class.isAssignableFrom(classOf)) {
            object = SMBridge.lookupEnum((Class<Enum>) classOf, object.toString());

            if (object == null)
                return null;
        }

        else if (List.class.isAssignableFrom(classOf) && object instanceof List) {
            // Good

        } else if (Map.class.isAssignableFrom(classOf)) {
            // if (object instanceof Map) {
                // return (T) object;
                return (T) deserializeMap(object.toString());
            // }

            // throw new SerializeFailedException("Does not know how to turn " + object.getClass().getSimpleName() + " into a Map! (Keep in mind we can only serialize into Map<String, Object> Data: " + object);

        } else if (classOf.isArray()) {
            final Class<?> arrayType = classOf.getComponentType();
            T[] array;

            if (object instanceof List) {
                final List<?> rawList = (List<?>) object;
                array = (T[]) Array.newInstance(classOf.getComponentType(), rawList.size());

                for (int i = 0; i < rawList.size(); i++) {
                    final Object element = rawList.get(i);

                    array[i] = element == null ? null : (T) deserialize(arrayType, element);
                }
            }

            else {
                final Object[] rawArray = (Object[]) object;
                array = (T[]) Array.newInstance(classOf.getComponentType(), rawArray.length);

                for (int i = 0; i < array.length; i++)
                    array[i] = rawArray[i] == null ? null : (T) deserialize(classOf.getComponentType(), rawArray[i]);
            }

            return (T) array;

        }

        else if (classOf == Object.class) {
            // Good
        }

        else
            throw new SerializeFailedException("Does not know how to turn " + classOf + " into a serialized object from data: " + object);

        return (T) object;
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
     * Attempts to turn the given item or map into an item
     *
     * @param obj
     * @return
     */
    // private static ItemStack deserializeItemStack(@NonNull Mode mode, @NonNull Object obj) {
    //     try {

    //         if (obj instanceof ItemStack)
    //             return (ItemStack) obj;

    //         if (mode == Mode.JSON)
    //             return JsonItemStack.fromJson(obj.toString());

    //         final SerializedMap map = SerializedMap.of(obj);

    //         final ItemStack item = ItemStack.deserialize(map.asMap());
    //         final SerializedMap meta = map.getMap("meta");

    //         if (meta != null)
    //             try {
    //                 final Class<?> cl = SMBridge.getOBCClass("inventory." + (meta.containsKey("spawnedType") ? "CraftMetaSpawnEgg" : "CraftMetaItem"));
    //                 final Constructor<?> c = cl.getDeclaredConstructor(Map.class);
    //                 c.setAccessible(true);

    //                 final Object craftMeta = c.newInstance((Map<String, ?>) meta.serialize());

    //                 if (craftMeta instanceof ItemMeta)
    //                     item.setItemMeta((ItemMeta) craftMeta);

    //             } catch (final Throwable t) {

    //                 // We have to manually deserialize metadata :(
    //                 final ItemMeta itemMeta = item.getItemMeta();

    //                 final String display = meta.containsKey("display-name") ? meta.getString("display-name") : null;

    //                 if (display != null)
    //                     itemMeta.setDisplayName(display);

    //                 final List<String> lore = meta.containsKey("lore") ? meta.getStringList("lore") : null;

    //                 if (lore != null)
    //                     itemMeta.setLore(lore);

    //                 final SerializedMap enchants = meta.containsKey("enchants") ? meta.getMap("enchants") : null;

    //                 if (enchants != null)
    //                     for (final Map.Entry<String, Object> entry : enchants.entrySet()) {
    //                         final Enchantment enchantment = Enchantment.getByName(entry.getKey());
    //                         final int level = (int) entry.getValue();

    //                         itemMeta.addEnchant(enchantment, level, true);
    //                     }

    //                 final List<String> itemFlags = meta.containsKey("ItemFlags") ? meta.getStringList("ItemFlags") : null;

    //                 if (itemFlags != null)
    //                     for (final String flag : itemFlags)
    //                         try {
    //                             itemMeta.addItemFlags(ItemFlag.valueOf(flag));
    //                         } catch (final Exception ex) {
    //                             // Likely not MC compatible, ignore
    //                         }

    //                 item.setItemMeta(itemMeta);
    //             }

    //         return item;

    //     } catch (final Throwable t) {
    //         t.printStackTrace();

    //         return null;
    //     }
    // }

    /**
     * How should we de/serialize the objects in this class?
     */
    public enum Mode {
        JSON,
        YAML
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
}