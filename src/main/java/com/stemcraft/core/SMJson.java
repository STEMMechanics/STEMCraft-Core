package com.stemcraft.core;

import java.lang.reflect.Constructor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.stemcraft.STEMCraft;

public final class SMJson {
    private static Gson gson = null;

    private static Gson initalize() {
        if(gson == null) {
            GsonBuilder builder = new GsonBuilder()
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(JsonReader::nextInt)
                .setLenient();

            STEMCraft.iteratePluginFiles("com/stemcraft/core/adapters/", jar -> {
                String className = jar.getName();
        
                if (className.endsWith(".class")) {
                    try {
                        Class<?> classItem = Class.forName(
                            className.substring(0, className.length() - 6).replaceAll("/", ".")
                        );
        
                        if (SMJsonAdapter.class.isAssignableFrom(classItem)) {
                            @SuppressWarnings("unchecked")
                            Class<SMJsonAdapter> adapterClass = (Class<SMJsonAdapter>) classItem;
                            Constructor<SMJsonAdapter> constructor = adapterClass.getDeclaredConstructor();
                            SMJsonAdapter adapterInstance = constructor.newInstance();

                            builder.registerTypeAdapter(adapterInstance.adapterFor(), adapterInstance);
                            STEMCraft.info("JSON Adapter registered for " + adapterInstance.adapterFor().getSimpleName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // builder.registerTypeAdapter(ItemStack[].class, new SMAdapterItemStackArray());

            gson = builder.create();
        }

        return gson;
    }

    public static Gson getGson() {
        return initalize();
    }

    public static String toJson(Object object) {
        Gson gson = initalize();

        if(gson != null) {
            try {
                return gson.toJson(object);
            } catch(JsonIOException e) {
                throw new SMSerializeFailedException("JSON adapter missing for " + object.getClass().getSimpleName());
            } catch(Exception e) {
                throw new SMSerializeFailedException(e.getMessage());
            }
        } else {
            throw new SMSerializeFailedException("Could not initalize GSON");
        }
    }

    public static String toJson(Object object, Class<?> classOf) {
        Gson gson = initalize();

        if(gson != null) {
            try {
                return gson.toJson(object, classOf);
            } catch(JsonIOException e) {
                throw new SMSerializeFailedException("JSON adapter missing for " + object.getClass().getSimpleName());
            } catch(Exception e) {
                throw new SMSerializeFailedException(e.getMessage());
            }
        } else {
            throw new SMSerializeFailedException("Could not initalize GSON");
        }
    }

    public static <T> T fromJson(Class<T> classOf, String json) throws IllegalArgumentException {
        initalize();
        return gson.fromJson(json, classOf);
    }

    /**
     * Thrown when cannot serialize an object because an adapter is missing
     */
    public static class SMSerializeFailedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public SMSerializeFailedException(String reason) {
            super(reason);
        }
    }
}
