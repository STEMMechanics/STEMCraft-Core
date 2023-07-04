package com.stemcraft.database.migrations;

import java.sql.SQLException;
import com.stemcraft.database.SMDatabase;
import com.stemcraft.database.SMDatabaseMigration;

public class _230626143000_CreateMetaTable implements SMDatabaseMigration {
    
    public void up() throws SQLException {
        SMDatabase.prepareStatement(
            "CREATE TABLE IF NOT EXISTS meta (" +
                "name TEXT UNIQUE NOT NULL, " +
                "data BLOB)").executeUpdate();
    }

    public void down() {
        try {
            SMDatabase.prepareStatement(
                "DROP TABLE IF EXISTS meta").executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
