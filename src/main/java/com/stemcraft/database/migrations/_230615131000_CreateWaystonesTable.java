package com.stemcraft.database.migrations;

import java.sql.SQLException;
import com.stemcraft.database.SMDatabase;
import com.stemcraft.database.SMDatabaseMigration;

public class _230615131000_CreateWaystonesTable implements SMDatabaseMigration {
    
    public void up() throws SQLException {
        SMDatabase.prepareStatement(
            "CREATE TABLE IF NOT EXISTS waystones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "world TEXT NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "under_block TEXT NOT NULL)").executeUpdate();
    }

    public void down() {
        try {
            SMDatabase.prepareStatement(
                "DROP TABLE IF EXISTS waystones").executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
