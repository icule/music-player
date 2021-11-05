package org.icule.player.database;

import org.icule.player.configuration.ConfigurationManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(final ConfigurationManager configurationManager) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        String jdbcString = "jdbc:h2:" + configurationManager.getDatabasePath();
        connection = DriverManager.getConnection(jdbcString);
    }

    public void init() {

    }

    public void commit() throws DatabaseException {
        try {
            connection.commit();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to commit to database. Original Error: %s", e.getMessage());
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        }
        catch (SQLException e) {
            //there is nothing to do
        }
    }

    PreparedStatement getStatement(final String query) throws SQLException {
        return connection.prepareStatement(query);
    }
}
