package com.thorium.infrastructure.persistence.repository;

import com.thorium.infrastructure.persistence.SQLiteConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

abstract class AbstractRepository {

    protected final SQLiteConnectionProvider connectionProvider;

    protected AbstractRepository(SQLiteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected Connection connection() throws SQLException {
        return connectionProvider.getConnection();
    }

    protected void commit(Connection connection) throws SQLException {
        connection.commit();
    }
}
