package com.stemcraft.interfaces;

import java.sql.SQLException;

@FunctionalInterface
public interface SMSQLConsumer<T> {
    void accept(T t) throws SQLException;
}
