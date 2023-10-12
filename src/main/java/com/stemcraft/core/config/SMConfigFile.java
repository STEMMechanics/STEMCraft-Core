package com.stemcraft.core.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.stemcraft.STEMCraft;
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
    
    /**
     * Constructor
     * @param path
     */
    public SMConfigFile(String path) {
        try {
            InputStream defaultData = STEMCraft.getPlugin().getResource(path);
            if(defaultData == null) {
                defaultData = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            }

            file = YamlDocument.create(
                new File(STEMCraft.getPlugin().getDataFolder(), path), 
                defaultData,
                GeneralSettings.builder().setKeyFormat(KeyFormat.OBJECT).build(), 
                LoaderSettings.DEFAULT, 
                DumperSettings.DEFAULT, 
                UpdaterSettings.DEFAULT
            );
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save the config file.
     */
    public void save() {
        if(file != null) {
            try {
                file.save();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Reload the config file.
     */
    public void reload() {
        if(file != null) {
            try {
                file.reload();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Set default key value in config.
     * @param key
     * @param value
     */
    public void setDefault(String key, Object value) {
        setDefault(key, value, "");
    }

    /**
     * Set default key value in config.
     * @param key
     * @param value
     * @param comment
     */
    public void setDefault(String key, Object value, String comment) {
        if(file != null && !file.contains(key)) {
            file.set(key, value);

            if(comment != null && comment != "") {
                file.getBlock(key).addComment(comment);
            }
        }
    }

    /**
     * Check if specific key exists.
     * @param key
     * @return
     */
    public Boolean contains(String key) {
        if(file != null) {
            return file.contains(key);
        }

        return false;
    }

    /**
     * Get boolean value of key.
     * @param key
     * @return
     */
    public Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Get boolean value of key.
     * @param key
     * @param defValue
     * @return
     */
    public Boolean getBoolean(String key, Boolean defValue) {
        if(file != null) {
            return file.getBoolean(key, defValue);
        }

        return defValue;
    }

    /**
     * Get integer value of key.
     * @param key
     * @return
     */
    public Integer getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Get integer value of key.
     * @param key
     * @param defValue
     * @return
     */
    public Integer getInt(String key, Integer defValue) {
        if(file != null) {
            return file.getInt(key, defValue);
        }

        return defValue;
    }

    /**
     * Get integer list value of key.
     * @param key
     * @return
     */
    public List<Integer> getIntList(String key) {
        if(file != null) {
            return file.getIntList(key);
        }

        return new ArrayList<>();
    }

    /**
     * Get double value of key.
     * @param key
     * @return
     */
    public Double getDouble(String key) {
        return getDouble(key, 0d);
    }

    /**
     * Get double value of key.
     * @param key
     * @param defValue
     * @return
     */
    public Double getDouble(String key, Double defValue) {
        if(file != null) {
            return file.getDouble(key, defValue);
        }

        return defValue;
    }

    /**
     * Get double list value of key.
     * @param key
     * @return
     */
    public List<Double> getDoubleList(String key) {
        if(file != null) {
            return file.getDoubleList(key);
        }

        return new ArrayList<>();
    }

    /**
     * Get float value of key.
     * @param key
     * @return
     */
    public Float getFloat(String key) {
        return getFloat(key, 0f);
    }

    /**
     * Get float value of key.
     * @param key
     * @param defValue
     * @return
     */
    public Float getFloat(String key, Float defValue) {
        if(file != null) {
            return file.getFloat(key, defValue);
        }

        return defValue;
    }

    /**
     * Get float list value of key.
     * @param key
     * @return
     */
    public List<Float> getFloatList(String key) {
        if(file != null) {
            return file.getFloatList(key);
        }

        return new ArrayList<>();
    }

    /**
     * Get string value of key.
     * @param key
     * @return
     */
    public String getString(String key) {
        return getString(key, "");
    }

    /**
     * Get string value of key.
     * @param key
     * @param defValue
     * @return
     */
    public String getString(String key, String defValue) {
        if(file != null) {
            return file.getString(key, defValue);
        }

        return defValue;
    }

    /**
     * Get string list value of key.
     * @param key
     * @return
     */
    public List<String> getStringList(String key) {
        if(file != null) {
            return file.getStringList(key);
        }

        return new ArrayList<>();
    }
}
