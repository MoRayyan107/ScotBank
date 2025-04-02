package uk.co.asepstrath.bank.services.repository;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

abstract class BaseRepository {
    protected final Logger logger;

    protected BaseRepository(Logger logger) {
        this.logger = logger;
    }

    protected void executeUpdate(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
}
