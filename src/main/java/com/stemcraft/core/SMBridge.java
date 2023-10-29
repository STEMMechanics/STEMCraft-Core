package com.stemcraft.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.exception.SMBridgeException;
import lombok.NonNull;

/**
 * STEMCraft cross-version compatibility class.
 * 
 * Look up for many methods enabling the plugin to be compatible with later versions of the server.
 */
public final class SMBridge {
    /**
     * Return the server's command map
     * 
     * @return
     */
    public static CommandMap getCommandMap() {
        try {
            Server server = STEMCraft.getPlugin().getServer();
            final Field bukkitCommandMap = server.getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(server);
            return commandMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // public static CommandMap getCommandMap() {
    // final Class<?> craftServer = getOBCClass("CraftServer");

    // try {
    // return (SimpleCommandMap) craftServer.getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());

    // } catch (final ReflectiveOperationException ex) {

    // try {
    // return ReflectionUtil.getFieldContent(Bukkit.getServer(), "commandMap");

    // } catch (final Throwable ex2) {
    // throw new FoException(ex2, "Unable to get the command map");
    // }
    // }

    // try {
    // final Field bukkitCommandMap = this.plugin.getServer().getClass().getDeclaredField("commandMap");

    // bukkitCommandMap.setAccessible(true);
    // CommandMap commandMap = (CommandMap) bukkitCommandMap.get(this.plugin.getServer());
    // return commandMap;
    // } catch(Exception e) {
    // e.printStackTrace();
    // }

    // return null;

    // }

    public static String getName(final Entity entity) {
        try {
            return entity.getName();

        } catch (final NoSuchMethodError t) {
            return entity instanceof Player ? ((Player) entity).getName()
                : SMCommon.beautifyCapitalize(entity.getType());
        }
    }

    public static <E extends Enum<E>> E lookupEnumTransform(final Class<E> enumType, String name) {
        SMValid.checkNotNull(enumType, "Type missing for " + name);
        SMValid.checkNotNull(name, "Name missing for " + enumType);

        E result = lookupEnum(enumType, name);

        // Try making the enum uppercased
        if (result == null) {
            name = name.toUpperCase();

            result = lookupEnum(enumType, name);
        }

        // Try replacing spaces with underscores
        if (result == null) {
            name = name.replace(" ", "_");
            result = lookupEnum(enumType, name);
        }

        // Try crunching all underscores (were spaces) all together
        if (result == null) {
            result = lookupEnum(enumType, name.replace("_", ""));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E lookupEnum(final Class<E> enumType, final String name) {
        try {
            boolean hasKey = false;
            Method method = null;

            try {
                method = enumType.getDeclaredMethod("fromKey", String.class);

                if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
                    hasKey = true;
                }

            } catch (final Throwable t) {
                /* empty */
            }

            // Only invoke fromName from non-Bukkit API since this gives unexpected results
            if (method == null && !enumType.getName().contains("org.bukkit")) {
                try {
                    method = enumType.getDeclaredMethod("fromName", String.class);

                    if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()))
                        hasKey = true;

                } catch (final Throwable t) {
                    /* empty */
                }
            }

            if (method != null && hasKey) {
                return (E) method.invoke(null, name);
            }

            // Resort to enum name
            return Enum.valueOf(enumType, name);

        } catch (final IllegalArgumentException ex) {
            return null;

        } catch (final ReflectiveOperationException ex) {
            return null;
        }
    }

    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... args) {
        try {
            final Method method = clazz.getMethod(methodName, args);
            method.setAccessible(true);
            return method;
        } catch (final NoSuchMethodException e) {
            /* empty */
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeStatic(@NonNull final Method method, final Object... params) {
        try {
            SMValid.checkBoolean(Modifier.isStatic(method.getModifiers()),
                "Method " + method.getName() + " must be static to be invoked through invokeStatic with params: "
                    + SMCommon.join(params));

            return (T) method.invoke(null, params);

        } catch (final ReflectiveOperationException ex) {
            throw new SMBridgeException(ex,
                "Could not invoke static method " + method + " with params " + SMCommon.join(params, ", "));
        }
    }

    /**
     * Get an Enchantment object by name (string)
     * 
     * @param name The string name of the enchantment.
     * @return An Enchantment object or null if doesn't exist.
     */
    @SuppressWarnings("deprecation")
    public static Enchantment getEnchantment(String name) {
        if (SMMinecraftVersion.atLeast(SMMinecraftVersion.V.v1_13)) {
            NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
            return Enchantment.getByKey(key);
        }

        return Enchantment.getByName(name.toUpperCase());
    }
}
