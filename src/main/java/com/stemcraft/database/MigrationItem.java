package com.stemcraft.database;

public interface MigrationItem {
    public void up(DatabaseHandler database);
    public void down(DatabaseHandler database);
}
