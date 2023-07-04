package com.stemcraft.database.migrations;

import java.sql.SQLException;
import com.stemcraft.database.SMDatabase;
import com.stemcraft.database.SMDatabaseMigration;

public class _230601229800_CreateTpLocationsTable implements SMDatabaseMigration {
    
    public void up() throws SQLException {
        SMDatabase.prepareStatement(
            "CREATE TABLE IF NOT EXISTS tp_locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "world TEXT," +
                "x REAL," +
                "y REAL," +
                "z REAL," +
                "yaw REAL," +
                "pitch REAL)").executeUpdate();
    }

    public void down() {
        try {
            SMDatabase.prepareStatement(
                "DROP TABLE IF EXISTS tp_locations").executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
