package joamonca.reactinator.util.get;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Database implements AutoCloseable {
    // sql is scary eh
    Connection connection;
    private final ConcurrentHashMap<Long, Integer> guildChancesCache = new ConcurrentHashMap<>();

    // Allowed optout columns to prevent SQL injection on dynamic queries
    private static final Set<String> OPTOUT_COLUMNS = Set.of("mpreg", "react", "quote");

    // Guild statements
    private final PreparedStatement ensureGuildStmt;
    private final PreparedStatement getGuildChancesStmt;
    private final PreparedStatement setGuildChancesStmt;
    private final PreparedStatement setSlashVerStmt;
    private final PreparedStatement getSlashVerStmt;

    // Channel statements
    private final PreparedStatement ensureChannelStmt;
    private final PreparedStatement getChannelChancesStmt;
    private final PreparedStatement setChannelChancesStmt;
    private final PreparedStatement getAllChannelChancesStmt;

    // User statements
    private final PreparedStatement ensureUserStmt;
    private final PreparedStatement isBlacklistedStmt;
    private final PreparedStatement setBlacklistedStmt;

    // Optout statements
    private final PreparedStatement ensureOptoutsStmt;

    // Emoji statements
    private final PreparedStatement ensureEmojiStmt;
    private final PreparedStatement isEmojiBlacklistedStmt;
    private final PreparedStatement setEmojiBlacklistedStmt;
    private final PreparedStatement incrementEmojiUsageStmt;
    private final PreparedStatement getEmojiLeaderboardStmt;

    // Message statements
    private final PreparedStatement insertMessageStmt;

    // Reaction statements
    private final PreparedStatement upsertReactionStmt;
    private final PreparedStatement getReactionCountStmt;
    private final PreparedStatement getReactLeaderboardStmt;
    private final PreparedStatement getReactionEmojiStmt;

    // Reactboard statements
    private final PreparedStatement setReactboardStmt;
    private final PreparedStatement getReactboardForGuildStmt;
    private final PreparedStatement removeReactboardStmt;

    // Quote tamper statements
    private final PreparedStatement insertQuoteTamperStmt;
    private final PreparedStatement getQuoteTamperStmt;

    public Database(String uri) throws SQLException {
        connection = DriverManager.getConnection(uri);

        // Guild
        ensureGuildStmt = connection.prepareStatement(
                "INSERT IGNORE INTO GUILDS (id, chances) VALUES (?, 1)");
        getGuildChancesStmt = connection.prepareStatement(
                "SELECT chances FROM GUILDS WHERE id = ?");
        setGuildChancesStmt = connection.prepareStatement(
                "UPDATE GUILDS SET chances = ? WHERE id = ?");
        setSlashVerStmt = connection.prepareStatement(
                "UPDATE GUILDS SET slash_ver = ? WHERE id = ?");
        getSlashVerStmt = connection.prepareStatement(
                "SELECT slash_ver FROM GUILDS WHERE id = ?");

        // Channel
        ensureChannelStmt = connection.prepareStatement(
                "INSERT IGNORE INTO CHANNEL (id, guild) VALUES (?, ?)");
        getChannelChancesStmt = connection.prepareStatement(
                "SELECT chances FROM CHANNEL WHERE id = ?");
        setChannelChancesStmt = connection.prepareStatement(
                "UPDATE CHANNEL SET chances = ? WHERE id = ?");
        getAllChannelChancesStmt = connection.prepareStatement(
                "SELECT id, chances FROM CHANNEL WHERE guild = ? AND chances IS NOT NULL");

        // User
        ensureUserStmt = connection.prepareStatement(
                "INSERT IGNORE INTO USERS (id) VALUES (?)");
        isBlacklistedStmt = connection.prepareStatement(
                "SELECT blacklisted FROM USERS WHERE id = ?");
        setBlacklistedStmt = connection.prepareStatement(
                "UPDATE USERS SET blacklisted = ? WHERE id = ?");

        // Optout
        ensureOptoutsStmt = connection.prepareStatement(
                "INSERT IGNORE INTO OPTOUTS (id) VALUES (?)");

        // Emoji
        ensureEmojiStmt = connection.prepareStatement(
                "INSERT IGNORE INTO EMOJI (id, guild) VALUES (?, ?)");
        isEmojiBlacklistedStmt = connection.prepareStatement(
                "SELECT blacklisted FROM EMOJI WHERE id = ?");
        setEmojiBlacklistedStmt = connection.prepareStatement(
                "UPDATE EMOJI SET blacklisted = ? WHERE id = ?");
        incrementEmojiUsageStmt = connection.prepareStatement(
                "UPDATE EMOJI SET times_used = times_used + 1 WHERE id = ?");
        getEmojiLeaderboardStmt = connection.prepareStatement(
                "SELECT id, times_used FROM EMOJI WHERE guild = ? AND blacklisted = FALSE ORDER BY times_used DESC LIMIT ?");

        // Message
        insertMessageStmt = connection.prepareStatement(
                "INSERT IGNORE INTO MESSAGES (id, channel) VALUES (?, ?)");

        // Reaction
        upsertReactionStmt = connection.prepareStatement(
                "INSERT INTO REACTION (message, count, emoji) VALUES (?, 1, ?) " +
                "ON DUPLICATE KEY UPDATE count = count + 1");
        getReactionCountStmt = connection.prepareStatement(
                "SELECT count FROM REACTION WHERE message = ?");
        getReactLeaderboardStmt = connection.prepareStatement(
                "SELECT r.message, r.count, r.emoji, m.channel FROM REACTION r " +
                "JOIN MESSAGES m ON r.message = m.id " +
                "JOIN CHANNEL c ON m.channel = c.id " +
                "WHERE c.guild = ? " +
                "ORDER BY r.count DESC LIMIT ?");
        getReactionEmojiStmt = connection.prepareStatement(
                "SELECT emoji FROM REACTION WHERE message = ?");

        // Reactboard
        setReactboardStmt = connection.prepareStatement(
                "INSERT INTO REACTBOARD (channel, threshold) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE threshold = VALUES(threshold)");
        getReactboardForGuildStmt = connection.prepareStatement(
                "SELECT r.channel, r.threshold FROM REACTBOARD r " +
                "JOIN CHANNEL c ON r.channel = c.id WHERE c.guild = ? LIMIT 1");
        removeReactboardStmt = connection.prepareStatement(
                "DELETE FROM REACTBOARD WHERE channel = ?");

        // Quote tamper
        insertQuoteTamperStmt = connection.prepareStatement(
                "INSERT INTO QUOTE_TAMPER (user_id, message) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        getQuoteTamperStmt = connection.prepareStatement(
                "SELECT qt.user_id, qt.message, m.channel, c.guild " +
                "FROM QUOTE_TAMPER qt " +
                "JOIN MESSAGES m ON qt.message = m.id " +
                "JOIN CHANNEL c ON m.channel = c.id " +
                "WHERE qt.signature = ?");
    }

    public void ensureGuild(long guildId) {
        try {
            ensureGuildStmt.setLong(1, guildId);
            ensureGuildStmt.executeUpdate();
            guildChancesCache.putIfAbsent(guildId, 1); // default 1%
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getGuildChances(long guildId) {
        Integer cached = guildChancesCache.get(guildId);
        if (cached != null) return cached;

        try {
            getGuildChancesStmt.setLong(1, guildId);
            try (ResultSet rs = getGuildChancesStmt.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt("chances");
                    guildChancesCache.put(guildId, value);
                    return value;
                }
            }
            return -1; // Default value if guild not found
        } catch (SQLException e) {
            e.printStackTrace();
            return -2; // Default value on error, -1 is used for not found
        }
    }

    public boolean setGuildChances(long guildId, int chances) {
        try {
            setGuildChancesStmt.setInt(1, chances);
            setGuildChancesStmt.setLong(2, guildId);
            if (setGuildChancesStmt.executeUpdate() > 0) {
                guildChancesCache.put(guildId, chances);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setSlashVer(long guildId, String version) {
        try {
            setSlashVerStmt.setString(1, version);
            setSlashVerStmt.setLong(2, guildId);
            setSlashVerStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getSlashVer(long guildId) {
        try {
            getSlashVerStmt.setLong(1, guildId);
            try (ResultSet rs = getSlashVerStmt.executeQuery()) {
                if (rs.next()) return rs.getString("slash_ver");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void ensureChannel(long channelId, long guildId) {
        try {
            ensureChannelStmt.setLong(1, channelId);
            ensureChannelStmt.setLong(2, guildId);
            ensureChannelStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getChannelChances(long channelId) {
        try {
            getChannelChancesStmt.setLong(1, channelId);
            try (ResultSet rs = getChannelChancesStmt.executeQuery()) {
                if (rs.next()) {
                    int val = rs.getInt("chances");
                    return rs.wasNull() ? -1 : val;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // not found = use guild default
    }

    public boolean setChannelChances(long channelId, int chances) {
        try {
            setChannelChancesStmt.setInt(1, chances);
            setChannelChancesStmt.setLong(2, channelId);
            return setChannelChancesStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void ensureUser(long userId) {
        try {
            ensureUserStmt.setLong(1, userId);
            ensureUserStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBlacklisted(long userId) {
        try {
            isBlacklistedStmt.setLong(1, userId);
            try (ResultSet rs = isBlacklistedStmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean("blacklisted");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // not found = not blacklisted
    }

    public boolean setBlacklisted(long userId, boolean blacklisted) {
        try {
            setBlacklistedStmt.setBoolean(1, blacklisted);
            setBlacklistedStmt.setLong(2, userId);
            return setBlacklistedStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void ensureOptouts(long userId) {
        ensureUser(userId);
        try {
            ensureOptoutsStmt.setLong(1, userId);
            ensureOptoutsStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getOptout(long userId, String column) {
        if (!OPTOUT_COLUMNS.contains(column)) return false;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT " + column + " FROM OPTOUTS WHERE id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // not found = not opted out
    }

    public boolean setOptout(long userId, String column, boolean value) {
        if (!OPTOUT_COLUMNS.contains(column)) return false;
        ensureOptouts(userId);
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE OPTOUTS SET " + column + " = ? WHERE id = ?")) {
            stmt.setBoolean(1, value);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void ensureEmoji(long emojiId, long guildId) {
        try {
            ensureEmojiStmt.setLong(1, emojiId);
            ensureEmojiStmt.setLong(2, guildId);
            ensureEmojiStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmojiBlacklisted(long emojiId) {
        try {
            isEmojiBlacklistedStmt.setLong(1, emojiId);
            try (ResultSet rs = isEmojiBlacklistedStmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean("blacklisted");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // not found = not blacklisted
    }

    public boolean setEmojiBlacklisted(long emojiId, boolean blacklisted) {
        try {
            setEmojiBlacklistedStmt.setBoolean(1, blacklisted);
            setEmojiBlacklistedStmt.setLong(2, emojiId);
            return setEmojiBlacklistedStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void incrementEmojiUsage(long emojiId) {
        try {
            incrementEmojiUsageStmt.setLong(1, emojiId);
            incrementEmojiUsageStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<long[]> getEmojiLeaderboard(long guildId, int limit) {
        List<long[]> results = new ArrayList<>();
        try {
            getEmojiLeaderboardStmt.setLong(1, guildId);
            getEmojiLeaderboardStmt.setInt(2, limit);
            try (ResultSet rs = getEmojiLeaderboardStmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new long[]{ rs.getLong("id"), rs.getLong("times_used") });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    public List<ReactLeaderboardEntry> getReactLeaderboard(long guildId, int limit) {
        List<ReactLeaderboardEntry> results = new ArrayList<>();
        try {
            getReactLeaderboardStmt.setLong(1, guildId);
            getReactLeaderboardStmt.setInt(2, limit);
            try (ResultSet rs = getReactLeaderboardStmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new ReactLeaderboardEntry(
                            rs.getLong("message"),
                            rs.getLong("channel"),
                            rs.getInt("count"),
                            rs.getLong("emoji")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public void insertMessage(long messageId, long channelId) {
        try {
            insertMessageStmt.setLong(1, messageId);
            insertMessageStmt.setLong(2, channelId);
            insertMessageStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void upsertReaction(long messageId, long emojiId) {
        try {
            upsertReactionStmt.setLong(1, messageId);
            upsertReactionStmt.setLong(2, emojiId);
            upsertReactionStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getReactionCount(long messageId) {
        try {
            getReactionCountStmt.setLong(1, messageId);
            try (ResultSet rs = getReactionCountStmt.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Long getReactionEmoji(long messageId) {
        try {
            getReactionEmojiStmt.setLong(1, messageId);
            try (ResultSet rs = getReactionEmojiStmt.executeQuery()) {
                if (rs.next()) return rs.getLong("emoji");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setReactboard(long channelId, int threshold) {
        try {
            setReactboardStmt.setLong(1, channelId);
            setReactboardStmt.setInt(2, threshold);
            setReactboardStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ReactboardEntry getReactboardForGuild(long guildId) {
        try {
            getReactboardForGuildStmt.setLong(1, guildId);
            try (ResultSet rs = getReactboardForGuildStmt.executeQuery()) {
                if (rs.next()) {
                    return new ReactboardEntry(rs.getLong("channel"), rs.getInt("threshold"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeReactboard(long channelId) {
        try {
            removeReactboardStmt.setLong(1, channelId);
            removeReactboardStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long insertQuoteTamper(long userId, long messageId, long channelId, long guildId) {
        try {
            ensureUser(userId);
            ensureGuild(guildId);
            ensureChannel(channelId, guildId);
            insertMessage(messageId, channelId);
            insertQuoteTamperStmt.setLong(1, userId);
            insertQuoteTamperStmt.setLong(2, messageId);
            insertQuoteTamperStmt.executeUpdate();
            try (ResultSet keys = insertQuoteTamperStmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public QuoteTamperInfo getQuoteTamper(long signature) {
        try {
            getQuoteTamperStmt.setLong(1, signature);
            try (ResultSet rs = getQuoteTamperStmt.executeQuery()) {
                if (rs.next()) {
                    return new QuoteTamperInfo(
                            rs.getLong("user_id"),
                            rs.getLong("message"),
                            rs.getLong("channel"),
                            rs.getLong("guild")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            PreparedStatement[] stmts = {
                    ensureGuildStmt, getGuildChancesStmt, setGuildChancesStmt, setSlashVerStmt, getSlashVerStmt,
                    ensureChannelStmt, getChannelChancesStmt, setChannelChancesStmt, getAllChannelChancesStmt,
                    ensureUserStmt, isBlacklistedStmt, setBlacklistedStmt,
                    ensureOptoutsStmt,
                    ensureEmojiStmt, isEmojiBlacklistedStmt, setEmojiBlacklistedStmt,
                    incrementEmojiUsageStmt, getEmojiLeaderboardStmt,
                    insertMessageStmt,
                    upsertReactionStmt, getReactionCountStmt, getReactLeaderboardStmt, getReactionEmojiStmt,
                    setReactboardStmt, getReactboardForGuildStmt, removeReactboardStmt,
                    insertQuoteTamperStmt, getQuoteTamperStmt
            };
            for (PreparedStatement stmt : stmts) {
                if (stmt != null) stmt.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
