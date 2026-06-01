package joamonca.reactinator.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class ReactDB implements AutoCloseable {
    Connection connection;
    private final PreparedStatement getChancesStmt;
    private final PreparedStatement setChancesStmt;
    private final ConcurrentHashMap<Long, Float> chancesCache = new ConcurrentHashMap<>();

    public ReactDB(String uri) throws SQLException {
        connection = DriverManager.getConnection(uri);
        getChancesStmt = connection.prepareStatement("SELECT chances FROM guild_chances WHERE id = ?");
        setChancesStmt = connection.prepareStatement(
                "INSERT INTO guild_chances (id, chances) VALUES (?, ?) ON DUPLICATE KEY UPDATE chances = VALUES(chances)");
    }

    public float getChances(Long guildId) {
        Float cached = chancesCache.get(guildId);
        if (cached != null) {
            return cached;
        }

        try {
            getChancesStmt.setLong(1, guildId);
            try (ResultSet resultSet = getChancesStmt.executeQuery()) {
                if (resultSet.next()) {
                    float value = resultSet.getFloat("chances");
                    chancesCache.put(guildId, value);
                    return value;
                }
            }
            return -1f; // Default value if guild not found
        } catch (SQLException e) {
            e.printStackTrace();
            return -2f; // Default value on error, -1 is used for not found
        }
    }

    public boolean setChances(Long guildId, float chances) {
        try {
            setChancesStmt.setLong(1, guildId);
            setChancesStmt.setFloat(2, chances);
            int rowsAffected = setChancesStmt.executeUpdate();
            if (rowsAffected > 0) {
                chancesCache.put(guildId, chances);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (getChancesStmt != null) getChancesStmt.close();
            if (setChancesStmt != null) setChancesStmt.close();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

