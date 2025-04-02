package uk.co.asepstrath.bank.services.repository;

import org.slf4j.Logger;
import uk.co.asepstrath.bank.Business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The Business repository service
 */
public class BusinessRepository extends BaseRepository {

    private static final String SQL_CREATE_TABLE = """
                CREATE TABLE Businesses (
                    BusinessID VARCHAR(255) NOT NULL,
                    BusinessName VARCHAR(255) NOT NULL,
                    Category VARCHAR(255) NOT NULL,
                    Sanctioned BIT NOT NULL,
                    PRIMARY KEY (BusinessID)
                )
            """;

    private static final String SQL_INSERT_BUSINESS =
            "INSERT INTO Businesses (BusinessID, BusinessName, Category, Sanctioned ) VALUES (?, ?, ?, ?)";

    public BusinessRepository(Logger logger) {
        super(logger);
    }

    /**
     * Creates the Business table
     *
     * @param connection Database connection
     * @throws SQLException Database connection failure
     */
    public void createTable(Connection connection) throws SQLException {
        executeUpdate(connection, SQL_CREATE_TABLE);
    }

    /**
     * Inserts a Business into the Business table
     *
     * @param connection Database Connection
     * @param business   A Business object
     * @throws SQLException Database connection failure
     */
    public void insert(Connection connection, Business business) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_BUSINESS)) {
            preparedStatement.setString(1, business.getID());
            preparedStatement.setString(2, business.getName());
            preparedStatement.setString(3, business.getCategory());
            preparedStatement.setBoolean(4, business.isSanctioned());
            preparedStatement.executeUpdate();
        }
    }


}
