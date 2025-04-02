package uk.co.asepstrath.bank.services.repository;

import org.slf4j.Logger;
import uk.co.asepstrath.bank.Reward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Rewards repository service
 */
public class RewardRepository extends BaseRepository {

    private static final String SQL_CREATE_TABLE_REWARDS = """
            CREATE TABLE Rewards (
            Name TEXT PRIMARY KEY NOT NULL,
            Description TEXT,
            RewardValue DECIMAL(5,2) NOT NULL,
            Chance DECIMAL(5,2) NOT NULL)
            """;

    private static final String SQL_GET_ALL_REWARDS =
            "SELECT Name, Description, RewardValue, Chance FROM Rewards";


    public RewardRepository(Logger logger) {
        super(logger);
    }

    /**
     * Creates the rewards tables
     *
     * @param connection Database connection
     * @throws SQLException Database connection failure
     */
    public void createTable(Connection connection) throws SQLException {
        executeUpdate(connection, SQL_CREATE_TABLE_REWARDS);
    }

    /**
     * Inserts a reward into the database
     *
     * @param connection Database connection
     * @param reward     Reward object to insert
     * @throws SQLException Database failure
     */
    public void insert(Connection connection, Reward reward) throws SQLException {
        String sql = "INSERT INTO Rewards (Name, Description, RewardValue, Chance) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reward.getName());
            stmt.setString(2, reward.getDescription());
            stmt.setBigDecimal(3, reward.getValue());
            stmt.setDouble(4, reward.getChance());
            stmt.executeUpdate();
            logger.info("Inserted reward: {}", reward.getName());
        }
    }

    /**
     * Retrieves all available rewards from the database
     *
     * @param connection Database connection
     * @return List of rewards
     * @throws SQLException Database failure
     */
    public List<Reward> getAllRewards(Connection connection) throws SQLException {
        List<Reward> rewards = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_ALL_REWARDS);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rewards.add(new Reward(
                        resultSet.getString("Name"),
                        resultSet.getString("Description"),
                        resultSet.getBigDecimal("RewardValue"),
                        resultSet.getDouble("Chance")
                ));
            }
        }
        return rewards;
    }


}
