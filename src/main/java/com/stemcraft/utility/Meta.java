package com.stemcraft.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.stemcraft.database.SMDatabase;

public class Meta {
    private static final Map<String, String> cache = new HashMap<>();

    public static String getString(String metaName, String metaDefault) {
        if (cache.containsKey(metaName)) {
            return cache.get(metaName);
        }

        String metaValue = metaDefault;

        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "SELECT data FROM meta WHERE name = ? LIMIT 1");
            statement.setString(1, metaName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                metaValue = resultSet.getString("data");
                cache.put(metaName, metaValue);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return metaValue;
    }

    public static Boolean getBoolean(String metaName, Boolean metaDefault) {
        String metaValue = getString(metaName, metaDefault.toString());
        return Boolean.parseBoolean(metaValue);
    }

    public static Integer getInteger(String metaName, Integer metaDefault) {
        String metaValue = getString(metaName, metaDefault.toString());
        return Integer.parseInt(metaValue);
    }

    public static Object getObject(String metaName) {
        String serializedData = getString(metaName, "");

        if (serializedData.length() > 0) {
            try {
                byte[] data = Base64.getDecoder().decode(serializedData);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Object deserializedObject = objectInputStream.readObject();
                objectInputStream.close();
                return deserializedObject;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void setString(String metaName, String metaValue) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "DELETE FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            statement.executeUpdate();

            statement = SMDatabase.prepareStatement(
                    "INSERT INTO meta (name, data) VALUES (?, ?)");
            statement.setString(1, metaName);
            statement.setString(2, metaValue);
            statement.executeUpdate();

            statement.close();

            cache.put(metaName, metaValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setBoolean(String metaName, Boolean metaValue) {
        setString(metaName, metaValue.toString());
    }

    public static void setInteger(String metaName, Integer metaValue) {
        setString(metaName, metaValue.toString());
    }

    public static void setObject(String metaName, Object metaValue) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(metaValue);
            objectOutputStream.close();
            setString(metaName, Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clear(String metaName) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "DELETE FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            statement.executeUpdate();

            statement.close();

            cache.remove(metaName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Boolean exists(String metaName) {
        if (cache.containsKey(metaName)) {
            return true;
        }

        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "SELECT COUNT(*) FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                if (count > 0) {
                    cache.put(metaName, "");
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
