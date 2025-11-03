package joamonca.reactinator.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReactDB {
    Connection connection;
    public ReactDB(String uri) throws SQLException {
        connection = DriverManager.getConnection(uri);
    }

    public float getChances(Long guildId) {
        try {
            var statement = connection.prepareStatement("SELECT chances FROM guild_chances WHERE id = %s".formatted(guildId));
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getFloat("chances");
            }
            return -1f; // Default value if user not found
        } catch (SQLException e) {
            e.printStackTrace();
            return -2f; // Default value on error, -1 is used for not found
        }
    }

    public boolean setChances(Long guildId, float chances) {
        String sql = "INSERT INTO guild_chances (id, chances) VALUES (?, ?) ON DUPLICATE KEY UPDATE chances = VALUES(chances)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, guildId);
            statement.setFloat(2, chances);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
