package com.stemcraft.core.interfaces;

import java.sql.SQLException;

@FunctionalInterface
public interface SMSQLConsumer {
    void accept() throws SQLException;
}
