package com.stemcraft.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.stemcraft.core.exception.SMException;

/**
 * A helper class that allows features to store data in a persistent manner across server restarts.
 */
public class SMPersistent {
    /**
     * The meta data cache
     */
    private static final Map<String, String> dataCache = new HashMap<>();

    /**
     * If the Meta engine has been initalized.
     */
    private static boolean initalized = false;

    /**
     * Initalize the meta engine.
     */
    private static void initalize() {
        if (!initalized) {
            if (SMDatabase.connect()) {
                SMDatabase.runMigration("231027093900_UpdatePersistentTable", () -> {
                    PreparedStatement statement = SMDatabase.prepareStatement(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name='meta'");

                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        SMDatabase.prepareStatement(
                            "ALTER TABLE meta RENAME TO persistent").executeUpdate();
                    } else {
                        SMDatabase.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS persistent (" +
                                "name TEXT UNIQUE NOT NULL, " +
                                "data BLOB)")
                            .executeUpdate();
                    }
                });

                initalized = true;
            } else {
                throw new SMException("Cannot connect to Database");
            }
        }
    }

    /**
     * Get persistent value as Boolean.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param defaultValue The default value to return if the key does not exist.
     * @return The persistent value as a Boolean.
     */
    public static Boolean getBool(Object section, String key, Boolean defaultValue) {
        String value = getString(section, key, defaultValue.toString());
        return Boolean.parseBoolean(value);
    }

    /**
     * Get persistent value as Integer.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param defaultValue The default value to return if the key does not exist.
     * @return The persistent value as an Integer.
     */
    public static Integer getInt(Object section, String key, Integer defaultValue) {
        String value = getString(section, key, defaultValue.toString());
        return Integer.parseInt(value);
    }

    /**
     * Get persistent value as Float.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param defaultValue The default value to return if the key does not exist.
     * @return The persistent value as a Float.
     */
    public static Float getFloat(Object section, String key, Float defaultValue) {
        String value = getString(section, key, defaultValue.toString());
        return Float.parseFloat(value);
    }

    /**
     * Get persistent value as Double.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param defaultValue The default value to return if the key does not exist.
     * @return The persistent value as a Double.
     */
    public static Double getDouble(Object section, String key, Double defaultValue) {
        String value = getString(section, key, defaultValue.toString());
        return Double.parseDouble(value);
    }

    /**
     * Get persistent value as Object.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param classOf The class type of the value.
     * @return The persistent value as an Object of type T.
     */
    public static <T> T getObject(Object section, String key, Class<T> classOf) {
        String value = getString(section, key, "");

        if (!value.isEmpty()) {
            try {
                return SMJson.fromJson(classOf, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Get persistent value as String.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param defaultValue The default value to return if the key does not exist.
     * @return The persistent value as a String.
     */
    public static String getString(Object section, String key, String defaultValue) {
        Boolean found = false;
        String value = "";
        String sectionNameKey = getObjectName(section) + "-" + key;

        if (dataCache.containsKey(sectionNameKey)) {
            value = dataCache.get(sectionNameKey);
            found = true;
        } else {
            try {
                initalize();
                PreparedStatement statement = SMDatabase.prepareStatement(
                    "SELECT data FROM persistent WHERE name = ? LIMIT 1");
                statement.setString(1, sectionNameKey);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    value = resultSet.getString("data");
                    dataCache.put(sectionNameKey, value);
                    found = true;
                }

                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (found) {
            try {
                return value;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        dataCache.put(sectionNameKey, defaultValue);
        return defaultValue;
    }

    /**
     * Set the persistent value.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param value The value to set.
     */
    public static void set(Object section, String key, Object value) {
        set(section, key, value, value.getClass());
    }

    /**
     * Set the persistent value with specified class type.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @param value The value to set.
     * @param classOf The class type of the value.
     */
    public static void set(Object section, String key, Object value, Class<?> classOf) {
        String sectionNameKey = getObjectName(section) + "-" + key;

        try {
            String valueString = "";

            if (classOf == null) {
                valueString = SMJson.toJson(value);
            } else {
                valueString = SMJson.toJson(value, classOf);
            }

            initalize();
            PreparedStatement statement = SMDatabase.prepareStatement(
                "DELETE FROM persistent WHERE name = ?");
            statement.setString(1, sectionNameKey);
            statement.executeUpdate();

            statement = SMDatabase.prepareStatement(
                "INSERT INTO persistent (name, data) VALUES (?, ?)");
            statement.setString(1, sectionNameKey);
            statement.setString(2, valueString);
            statement.executeUpdate();

            statement.close();

            dataCache.put(sectionNameKey, valueString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear a specific persistent item.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value to clear.
     */
    public static void clear(Object section, String key) {
        String sectionNameKey = getObjectName(section) + "-" + key;

        try {
            initalize();
            PreparedStatement statement = SMDatabase.prepareStatement(
                "DELETE FROM persistent WHERE name = ?");
            statement.setString(1, sectionNameKey);
            statement.executeUpdate();

            statement.close();

            dataCache.remove(sectionNameKey);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a specific persistent exists.
     *
     * @param section The section object or string.
     * @param key The key for the persistent value.
     * @return True if the persistent value exists, otherwise false.
     */
    public static Boolean exists(Object section, String key) {
        String sectionNameKey = getObjectName(section) + "-" + key;

        if (dataCache.containsKey(sectionNameKey)) {
            return true;
        }

        try {
            initalize();
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT COUNT(*) FROM persistent WHERE name = ?");
            statement.setString(1, sectionNameKey);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                if (count > 0) {
                    dataCache.put(sectionNameKey, "");
                    return true;
                }
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get a list of keys for a section.
     * 
     * @param section The section object or string.
     * @return A list of keys for the specified section.
     */
    public List<String> keys(Object section) {
        String sectionName = getObjectName(section);
        List<String> keys = new ArrayList<>();

        try {
            initalize();
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT `name` FROM persistent WHERE name LIKE ?");
            statement.setString(1, sectionName + "-%");
            ResultSet resultSet = statement.executeQuery();
            int sectionNameLength = sectionName.length() + 1;

            while (resultSet.next()) {
                keys.add(resultSet.getString(1).substring(sectionNameLength));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return keys;
    }

    /**
     * Transform a section object into a name.
     * 
     * @param section The section object or string.
     * @return The name of the object.
     */
    private static String getObjectName(Object section) {
        String name;
        if (section == null) {
            name = "unknown";
        } else if (section instanceof String) {
            name = (String) section;
        } else if (section instanceof Class) {
            name = ((Class<?>) section).getSimpleName();
        } else {
            name = section.getClass().getSimpleName();
        }

        return name;
    }
}
