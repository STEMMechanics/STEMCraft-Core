package com.stemcraft.database.migrations;

import com.stemcraft.database.DatabaseHandler;
import com.stemcraft.database.MigrationItem;

public class _230601229800_CreateTpLocationsTable implements MigrationItem {
    
    public void up(DatabaseHandler database) {
        try {
            database.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tp_locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "world TEXT," +
                    "x REAL," +
                    "y REAL," +
                    "z REAL," +
                    "yaw REAL," +
                    "pitch REAL)").executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void down(DatabaseHandler database) {
        try {
            database.prepareStatement(
                "DROP TABLE IF EXISTS tp_locations").executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
