package com.stemcraft.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.stemcraft.core.SMMinecraftVersion.V;

public class SMReflection {
    /**
     * The full package name for NMS
     */
    public static final String NMS = "net.minecraft.server";

    /**
     * The package name for Craftbukkit
     */
    public static final String CRAFTBUKKIT = "org.bukkit.craftbukkit";

    /**
     * Reflection utilizes a simple cache for fastest performance
     */
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    
    /**
     * Represents an exception during reflection operation
     */
    public static final class ReflectionException extends RuntimeException {
        public ReflectionException(final String message) {
            super(message);
        }

        public ReflectionException(final Throwable ex, final String message) {
            super(message, ex);
        }
    }
    
    /**
     * Find a class automatically for older MC version (such as type EntityPlayer for oldName
     * and we automatically find the proper NMS import) or if MC 1.17+ is used then type
     * the full class path such as net.minecraft.server.level.EntityPlayer and we use that instead.
     *
     * @param oldName
     * @param fullName1_17
     * @return
     */
    public static Class<?> getNMSClass(String oldName, String fullName1_17) {
        if(SMMinecraftVersion.atLeast(V.v1_17)) {
            return lookupClass(fullName1_17);
        } else {
            String version = SMMinecraftVersion.getServerVersion();

            if (!version.isEmpty())
                version += ".";

            return lookupClass(NMS + "." + version + oldName);
        }
    }

    /**
     * Find a class in org.bukkit.craftbukkit package, adding the version
     * automatically
     *
     * @param name
     * @return
     */
    public static Class<?> getOBCClass(final String name) {
        String version = SMMinecraftVersion.getServerVersion();

        if (!version.isEmpty())
            version += ".";

        return SMReflection.lookupClass(CRAFTBUKKIT + "." + version + name);
    }

    /**
     * Wrapper for Class.forName
     *
     * @param <T>
     * @param path
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> lookupClass(final String path) {
        if (classCache.containsKey(path)) {
            return (Class<T>) classCache.get(path);
        }

        try {
            final Class<?> clazz = Class.forName(path);

            classCache.put(path, clazz);

            return (Class<T>) clazz;
        } catch (final ClassNotFoundException ex) {
            throw new ReflectionException("Could not find class: " + path);
        }
    }
}
