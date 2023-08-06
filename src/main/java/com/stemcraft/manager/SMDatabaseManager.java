package com.stemcraft.manager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.sqlite.SQLiteDataSource;
import com.stemcraft.STEMCraft;
import com.stemcraft.interfaces.SMSQLConsumer;
import com.stemcraft.json.JSONObject;

public class SMDatabaseManager extends SMManager {
    private Connection connection = null;
    private String dbName = "database.db";
    private final Map<String, String> metaCache = new HashMap<>();

    @Override
    public void onEnable() {
        this.connect();
        if(this.isConnected()) {
            this.addMigration("230626143000_CreateMetaTable", (databaseManager) -> {
                databaseManager.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS meta (" +
                        "name TEXT UNIQUE NOT NULL, " +
                        "data BLOB)").executeUpdate();
            });
        }
    }

    @Override
    public void onDisable() {
        if(this.isConnected()) {
            this.disconnect();
        }
    }

    private void connect() {
        if(connection != null) {
            disconnect();
        }

        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:" + STEMCraft.getInstance().getDataFolder().getAbsolutePath() + "/" + this.dbName);
            connection = dataSource.getConnection();
            
            createMigrationTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createMigrationTable() {
        String tableName = "migration";
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT, migration TEXT)";

        try (Statement statement = this.connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean isConnected() {
        return this.connection != null;
    }

    public PreparedStatement prepareStatement(String statement) {
        if(connection != null) {
            try {
                return connection.prepareStatement(statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void addMigration(String name, SMSQLConsumer<SMDatabaseManager> callback) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM migration WHERE migration = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next()) {
                resultSet.close();
                statement.close();

                System.out.println("Running migration " + name);
                try {
                    callback.accept(this);
                } catch(SQLException e) {
                    e.printStackTrace();
                }
                
                statement = connection.prepareStatement("INSERT INTO migration (migration) VALUES (?)");
                statement.setString(1, name);
                statement.executeUpdate();
            }

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean getMeta(String name, Boolean defaultValue) {
        String metaValue = getMeta(name, defaultValue.toString());
        return Boolean.parseBoolean(metaValue);
    }

    public Integer getMeta(String name, Integer defaultValue) {
        String metaValue = getMeta(name, defaultValue.toString());
        return Integer.parseInt(metaValue);
    }

    public Object getMeta(String name) {
        String serializedData = getMeta(name, "");
        return new JSONObject(serializedData);
    }

    public String getMeta(String name, String defaultValue) {
        if (metaCache.containsKey(name)) {
            return metaCache.get(name);
        }

        String value = defaultValue;

        try {
            PreparedStatement statement = this.prepareStatement(
                    "SELECT data FROM meta WHERE name = ? LIMIT 1");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                value = resultSet.getString("data");
                metaCache.put(name, value);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return value;
    }

    public void setMeta(String metaName, Boolean metaValue) {
        setMeta(metaName, metaValue.toString());
    }

    public void setInteger(String metaName, Integer metaValue) {
        setMeta(metaName, metaValue.toString());
    }

    public void setMeta(String metaName, Object metaValue) {
        setMeta(metaName, new JSONObject(metaValue).toString());
    }

    public void setMeta(String metaName, String metaValue) {
        try {
            PreparedStatement statement = this.prepareStatement(
                    "DELETE FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            statement.executeUpdate();

            statement = this.prepareStatement(
                    "INSERT INTO meta (name, data) VALUES (?, ?)");
            statement.setString(1, metaName);
            statement.setString(2, metaValue);
            statement.executeUpdate();

            statement.close();

            metaCache.put(metaName, metaValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearMeta(String metaName) {
        try {
            PreparedStatement statement = this.prepareStatement(
                    "DELETE FROM meta WHERE name = ?");
            statement.setString(1, metaName);
            statement.executeUpdate();

            statement.close();

            metaCache.remove(metaName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean metaExists(String metaName) {
        if (metaCache.containsKey(metaName)) {
            return true;
        }

        try {
            PreparedStatement statement = this.prepareStatement(
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
