package com.stemcraft.database;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.stemcraft.STEMCraft;
import org.sqlite.SQLiteDataSource;

public class SMDatabase {
    private static Connection connection;
    private static String dbName = "database.db";

    public SMDatabase() {
        connection = null;
    }

    public static void connect() {
        if(connection != null) {
            disconnect();
        }

        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:" + STEMCraft.getInstance().getDataFolder().getAbsolutePath() + "/" + dbName);
            connection = dataSource.getConnection();
            
            createMigrationTable();
            runMigrations();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Boolean isConnected() {
        return connection != null;
    }

    public static PreparedStatement prepareStatement(String statement) {
        if(connection != null) {
            try {
                return connection.prepareStatement(statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static void createMigrationTable() {
        String tableName = "migration";
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT, migration TEXT)";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void runMigrations() {
        List<Class<?>> classMigrationList = STEMCraft.getClassList("com/stemcraft/database/migrations/", false);
        Collections.sort(classMigrationList, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> cls1, Class<?> cls2) {
                return cls1.getSimpleName().compareTo(cls2.getSimpleName());
            }
        });

        for (Class<?> classMigrationItem : classMigrationList) {
            try {
                String migrationName = classMigrationItem.getSimpleName().substring(1);
                PreparedStatement statement = connection.prepareStatement("SELECT id FROM migration WHERE migration = ?");
                statement.setString(1, migrationName);
                ResultSet resultSet = statement.executeQuery();

                if(!resultSet.next()) {
                    resultSet.close();
                    statement.close();

                    Constructor<?> constructor = classMigrationItem.getDeclaredConstructor();
                    SMDatabaseMigration migrationInstance = (SMDatabaseMigration) constructor.newInstance();

                    System.out.println("Running migration " + migrationName);
                    migrationInstance.up();
                    
                    statement = connection.prepareStatement("INSERT INTO migration (migration) VALUES (?)");
                    statement.setString(1, migrationName);
                    statement.executeUpdate();
                }

                resultSet.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void rollbackMigration() {

    }
}
