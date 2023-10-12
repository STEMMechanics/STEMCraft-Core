package com.stemcraft.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stemcraft.core.exception.SMException;

public class SMMeta {
    /**
     * The meta data cache
     */
    private static final Map<String, String> metaCache = new HashMap<>();

    /**
     * If the Meta engine has been initalized.
     */
    private static boolean initalized = false;

    /**
     * Initalize the meta engine.
     */
    private static void initalize() {
        if(!initalized) {
            if(SMDatabase.connect()) {
                SMDatabase.runMigration("230626143000_CreateMetaTable", () -> {
                    SMDatabase.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS meta (" +
                            "name TEXT UNIQUE NOT NULL, " +
                            "data BLOB)").executeUpdate();
                });

                initalized = true;
            } else {
                throw new SMException("Cannot connect to Database");
            }
        }
    }

    /**
     * Get Meta value as Boolean
     * @param name
     * @param defaultValue
     * @return
     */
    public static Boolean getBool(String name, Boolean defaultValue) {
        String metaValue = getString(name, defaultValue.toString());
        return Boolean.parseBoolean(metaValue);
    }

    /**
     * Get Meta value as Integer
     * @param name
     * @param defaultValue
     * @return
     */
    public static Integer getInt(String name, Integer defaultValue) {
        String metaValue = getString(name, defaultValue.toString());
        return Integer.parseInt(metaValue);
    }

    /**
     * Get Meta value as Float
     * @param name
     * @param defaultValue
     * @return
     */
    public static Float getFloat(String name, Float defaultValue) {
        String metaValue = getString(name, defaultValue.toString());
        return Float.parseFloat(metaValue);
    }

    /**
     * Get Meta value as Double
     * @param name
     * @param defaultValue
     * @return
     */
    public static Double getDouble(String name, Double defaultValue) {
        String metaValue = getString(name, defaultValue.toString());
        return Double.parseDouble(metaValue);
    }

    /**
     * Get Meta value as String
     * @param name
     * @return
     */
    public static String getString(String name, String defaultValue) {
        Boolean found = false;
        String metaValue = "";

        if (metaCache.containsKey(name)) {
            metaValue = metaCache.get(name);
            found = true;
        } else {
            try {
                initalize();
                PreparedStatement statement = SMDatabase.prepareStatement(
                        "SELECT data FROM meta WHERE name = ? LIMIT 1");
                statement.setString(1, name);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    metaValue = resultSet.getString("data");
                    metaCache.put(name, metaValue);
                    found = true;
                }

                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(found) {
            try {
                return metaValue;
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
        metaCache.put(name, defaultValue);
        return defaultValue;
    }

    /**
     * Set the meta value
     * @param metaName
     * @param metaValue
     */
    public static void set(String metaName, Object metaValue) {
        try {
            String valueString = new ObjectMapper().writeValueAsString(metaValue);

            initalize();
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "DELETE FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            statement.executeUpdate();

            statement = SMDatabase.prepareStatement(
                    "INSERT INTO meta (name, data) VALUES (?, ?)");
            statement.setString(1, metaName);
            statement.setString(2, valueString);
            statement.executeUpdate();

            statement.close();

            metaCache.put(metaName, valueString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear a specific meta item
     * @param metaName
     */
    public void clear(String metaName) {
        try {
            initalize();
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "DELETE FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            statement.executeUpdate();

            statement.close();

            metaCache.remove(metaName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a specific meta exists
     * @param metaName
     * @return
     */
    public Boolean exists(String metaName) {
        if (metaCache.containsKey(metaName)) {
            return true;
        }

        try {
            initalize();
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "SELECT COUNT(*) FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                if (count > 0) {
                    metaCache.put(metaName, "");
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
}
