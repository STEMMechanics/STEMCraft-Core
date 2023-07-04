package com.stemcraft.database;

import java.sql.SQLException;

public interface SMDatabaseMigration {
    public void up() throws SQLException;
    public void down();
}
