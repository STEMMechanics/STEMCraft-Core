package com.stemcraft.core.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings.KeyFormat;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

/**
 * Standard YAML Configuration File Class
 */
public class SMConfigFile {
    /*
     * YAML config file
     */
    private YamlDocument file = null;

    /*
     * YAML default config file
     */
    private YamlDocument defaults = null;

    /**
     * Constructor
     * 
     * @param path
     */
    public SMConfigFile(String path) {
        try {
            File filePath = new File(STEMCraft.getPlugin().getDataFolder(), path);
            if (!filePath.exists()) {
                InputStream defaultData = STEMCraft.getPlugin().getResource(path);
                if (defaultData != null) {
                    // Ensure the parent directory exists
                    filePath.getParentFile().mkdirs();

                    // Write the defaultData to the filePath
                    try {
                        Files.copy(defaultData, filePath.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            file = YamlDocument.create(
                filePath,
                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
                GeneralSettings.builder().setKeyFormat(KeyFormat.OBJECT).build(),
                LoaderSettings.DEFAULT,
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT);

            defaults = YamlDocument.create(STEMCraft.getPlugin().getResource(path));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save the config file.
     */
    public void save() {
        if (file != null) {
            try {
                file.save();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Reload the config file.
     */
    public void reload() {
        if (file != null) {
            try {
                file.reload();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Set key value in config.
     * 
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        set(key, value, "");
    }

    /**
     * Set key value in config.
     * 
     * @param key
     * @param value
     * @param comment
     */
    public void set(String key, Object value, String comment) {
        if (file != null) {
            file.set(key, value);

            if (comment != null && comment != "") {
                file.getBlock(key).addComment(comment);
            }
        }
    }

    /**
     * remove key value in config.
     * 
     * @param key
     */
    public void remove(String key) {
        if (file != null) {
            if (file.getBlock(key) != null) {
                file.getBlock(key).removeComments();
            }

            file.remove(key);
        }
    }

    /**
     * Check if specific key exists.
     * 
     * @param key
     * @return
     */
    public Boolean contains(String key) {
        if (file != null) {
            return file.contains(key);
        }

        return false;
    }

    /**
     * Get boolean value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public Boolean getBoolean(String key) {
        return getBoolean(key, getDefaultBoolean(key));
    }

    /**
     * Get boolean of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public Boolean getDefaultBoolean(String key) {
        if (defaults != null) {
            return defaults.getBoolean(key);
        }

        return null;
    }

    /**
     * Get boolean value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public Boolean getBoolean(String key, Boolean defValue) {
        if (file != null) {
            return file.getBoolean(key, defValue);
        }

        return defValue;
    }

    /**
     * Get integer value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public Integer getInt(String key) {
        return getInt(key, getDefaultInt(key));
    }

    /**
     * Get integer of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public Integer getDefaultInt(String key) {
        if (defaults != null) {
            return defaults.getInt(key);
        }

        return null;
    }

    /**
     * Get integer value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public Integer getInt(String key, Integer defValue) {
        if (file != null) {
            return file.getInt(key, defValue);
        }

        return defValue;
    }

    /**
     * Get integer list value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public List<Integer> getIntList(String key) {
        return file.getIntList(key, getDefaultIntList(key));
    }

    /**
     * Get integer list of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public List<Integer> getDefaultIntList(String key) {
        if (defaults != null) {
            return defaults.getIntList(key);
        }

        return null;
    }

    /**
     * Get integer list value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public List<Integer> getIntList(String key, List<Integer> defValue) {
        if (file != null) {
            return file.getIntList(key, defValue);
        }

        return defValue;
    }

    /**
     * Get double value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public Double getDouble(String key) {
        return getDouble(key, getDefaultDouble(key));
    }

    /**
     * Get double of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public Double getDefaultDouble(String key) {
        if (defaults != null) {
            return defaults.getDouble(key);
        }

        return null;
    }

    /**
     * Get double value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public Double getDouble(String key, Double defValue) {
        if (file != null) {
            return file.getDouble(key, defValue);
        }

        return defValue;
    }

    /**
     * Get double list value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public List<Double> getDoubleList(String key) {
        return file.getDoubleList(key, getDefaultDoubleList(key));
    }

    /**
     * Get double list of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public List<Double> getDefaultDoubleList(String key) {
        if (defaults != null) {
            return defaults.getDoubleList(key);
        }

        return null;
    }

    /**
     * Get double list value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public List<Double> getDoubleList(String key, List<Double> defValue) {
        if (file != null) {
            return file.getDoubleList(key, defValue);
        }

        return defValue;
    }

    /**
     * Get float value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public Float getFloat(String key) {
        return getFloat(key, getDefaultFloat(key));
    }

    /**
     * Get float of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public Float getDefaultFloat(String key) {
        if (defaults != null) {
            return defaults.getFloat(key);
        }

        return null;
    }

    /**
     * Get boolean value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public Float getFloat(String key, Float defValue) {
        if (file != null) {
            return file.getFloat(key, defValue);
        }

        return defValue;
    }

    /**
     * Get float list value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public List<Float> getFloatList(String key) {
        return file.getFloatList(key, getDefaultFloatList(key));
    }

    /**
     * Get float list of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public List<Float> getDefaultFloatList(String key) {
        if (defaults != null) {
            return defaults.getFloatList(key);
        }

        return null;
    }

    /**
     * Get float list value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public List<Float> getFloatList(String key, List<Float> defValue) {
        if (file != null) {
            return file.getFloatList(key, defValue);
        }

        return defValue;
    }

    /**
     * Get string value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public String getString(String key) {
        return getString(key, getDefaultString(key));
    }

    /**
     * Get string of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public String getDefaultString(String key) {
        if (defaults != null) {
            return defaults.getString(key);
        }

        return null;
    }

    /**
     * Get string value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public String getString(String key, String defValue) {
        if (file != null) {
            return file.getString(key, defValue);
        }

        return defValue;
    }

    /**
     * Get stringlist value of key. If does not exist, returns the default or null.
     * 
     * @param key The key to retrieve the value.
     * @return The key value, default or null.
     */
    public List<String> getStringList(String key) {
        return file.getStringList(key, getDefaultStringList(key));
    }

    /**
     * Get string list of the default value of a key.
     * 
     * @param key The key to retrieve the default value.
     * @return The key value or null.
     */
    public List<String> getDefaultStringList(String key) {
        if (defaults != null) {
            return defaults.getStringList(key);
        }

        return null;
    }

    /**
     * Get string list value of key. If does not exist, returns defValue or null.
     * 
     * @param key The key to retrieve the value.
     * @param defValue The default value to return if not existant.
     * @return The key value or defValue.
     */
    public List<String> getStringList(String key, List<String> defValue) {
        if (file != null) {
            return file.getStringList(key, defValue);
        }

        return defValue;
    }

    /**
     * Fetches a map from the file based on the given key and filters it to contain only String values.
     *
     * @param key The key to fetch the map from the file.
     * @return A map with string keys and Integer values.
     */
    public Map<String, String> getStringMap(String key) {
        Map<String, Object> valueMap =
            file != null ? file.getSection(key).getStringRouteMappedValues(false) : new HashMap<>();
        Map<String, String> resultMap = new HashMap<>();

        for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
            resultMap.put(entry.getKey().toString(), entry.getValue().toString());
        }

        return resultMap;
    }

    /**
     * Fetches a map from the file based on the given key and filters it to contain only Char values.
     *
     * @param key The key to fetch the map from the file.
     * @return A map with string keys and Integer values.
     */
    public Map<String, Character> getCharMap(String key) {
        Map<String, Object> valueMap =
            file != null ? file.getSection(key).getStringRouteMappedValues(false) : new HashMap<>();
        Map<String, Character> resultMap = new HashMap<>();

        for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
            String valueAsString = entry.getValue().toString();
            char value = valueAsString.length() > 0 ? valueAsString.charAt(0) : '\u0000';
            resultMap.put(entry.getKey().toString(), value);
        }

        return resultMap;
    }

    /**
     * Fetches a map from the file based on the given key and filters it to contain only Integer values.
     *
     * @param key The key to fetch the map from the file.
     * @return A map with string keys and Integer values.
     */
    public Map<String, Integer> getIntMap(String key) {
        Map<String, Object> valueMap =
            file != null ? file.getSection(key).getStringRouteMappedValues(false) : new HashMap<>();
        Map<String, Integer> resultMap = new HashMap<>();

        for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
            try {
                Integer value = Integer.parseInt(entry.getValue().toString());
                resultMap.put(entry.getKey().toString(), value);
            } catch (Exception e) {
                /* empty */
            }
        }

        return resultMap;
    }

    /**
     * Fetches a map from the file based on the given key and filters it to contain only Float values.
     *
     * @param key The key to fetch the map from the file.
     * @return A map with string keys and Float values.
     */
    public Map<String, Float> getFloatMap(String key) {
        Map<String, Object> valueMap =
            file != null ? file.getSection(key).getStringRouteMappedValues(false) : new HashMap<>();
        Map<String, Float> resultMap = new HashMap<>();

        for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
            try {
                Float value = Float.parseFloat(entry.getValue().toString());
                resultMap.put(entry.getKey().toString(), value);
            } catch (Exception e) {
                /* empty */
            }
        }

        return resultMap;
    }

    /**
     * Fetches a map from the file based on the given key and filters it to contain only Double values.
     *
     * @param key The key to fetch the map from the file.
     * @return A map with string keys and Double values.
     */
    public Map<String, Double> getDoubleMap(String key) {
        Map<String, Object> valueMap =
            file != null ? file.getSection(key).getStringRouteMappedValues(false) : new HashMap<>();
        Map<String, Double> resultMap = new HashMap<>();

        for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
            try {
                Double value = Double.parseDouble(entry.getValue().toString());
                resultMap.put(entry.getKey().toString(), value);
            } catch (Exception e) {
                /* empty */
            }
        }

        return resultMap;
    }

    /**
     * Fetches a list of keys from the root.
     *
     * @return A list of string keys.
     */
    public List<String> getKeys() {
        return getKeys(null);
    }

    /**
     * Fetches a list of keys from a path.
     *
     * @param key The path to fetch keys, null for root.
     * @return A list of string keys.
     */
    public List<String> getKeys(String key) {
        if (key == null) {
            return SMCommon.setToList(file.getKeys());
        }

        return SMCommon.setToList(file.getSection(key).getKeys());
    }

    /**
     * Fetches a list of default keys from the root.
     *
     * @return A list of string keys.
     */
    public List<String> getDefaultKeys() {
        return getDefaultKeys(null);
    }

    /**
     * Fetches a list of default keys from a path.
     *
     * @param key The path to fetch default keys, null for root.
     * @return A list of string keys.
     */
    public List<String> getDefaultKeys(String key) {
        if (key == null) {
            return SMCommon.setToList(defaults.getKeys());
        }

        return SMCommon.setToList(defaults.getSection(key).getKeys());
    }

    /**
     * Will add any missing default root values to a user configuration file.
     */
    public void addMissingDefaultValues() {
        addMissingDefaultValues(null);
    }

    /**
     * Will add any missing default values to a user configuration file.
     * 
     * @param key The key to add from, null for root
     */
    public void addMissingDefaultValues(String key) {
        List<String> defaultKeys = getDefaultKeys(key);
        if (defaultKeys == null) {
            return;
        }

        defaultKeys.removeAll(getKeys(key));

        for (String defaultKey : defaultKeys) {
            file.set(defaultKey, defaults.get(defaultKey));
        }

        try {
            file.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
