package com.stemcraft.database;

public interface SMDatabaseMigration {
    public void up();
    public void down();
}
