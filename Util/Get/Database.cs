using System.Collections.Concurrent;
using MySqlConnector;

namespace Reactinator.Util.Get;

public record ReactLeaderboardEntry(ulong MessageId, ulong ChannelId, int Count, ulong EmojiId);
public record ReactboardEntry(ulong Channel, int Threshold);
public record CountEntry(ulong Id, long Count);

public sealed class Database : IAsyncDisposable
{
    // sql is scary eh
    private readonly MySqlDataSource pool;

    // the hot path lives in memory, the db only hears about it when something changes
    private readonly ConcurrentDictionary<ulong, int> guildChances = new();
    private readonly ConcurrentDictionary<ulong, string?> slashVers = new();
    private readonly ConcurrentDictionary<ulong, int> channelChances = new();
    private readonly ConcurrentDictionary<ulong, bool> blacklistedUsers = new();
    private readonly ConcurrentDictionary<(ulong, string), bool> optouts = new();
    private readonly ConcurrentDictionary<ulong, bool> blacklistedEmojis = new();
    private readonly ConcurrentDictionary<ulong, HashSet<ulong>> guildBlacklistedEmojis = new();
    private readonly ConcurrentDictionary<ulong, ReactboardEntry?> reactboards = new();
    private readonly ConcurrentDictionary<ulong, byte> knownUsers = new();
    private readonly ConcurrentDictionary<ulong, byte> knownChannels = new();
    private readonly ConcurrentDictionary<ulong, byte> knownEmojis = new();
    private readonly ConcurrentDictionary<ulong, byte> warnedUsers = new();

    // no sql injection in this house
    public static readonly string[] OptoutColumns = ["mpreg", "react"];

    public Database(string uri)
    {
        pool = new MySqlDataSource(ToConnectionString(uri));
    }

    // the old .env has a jdbc uri, so we eat both that and plain connection strings
    // (cba to change the env but i'll say it's about backwards compatibility)
    private static string ToConnectionString(string uri)
    {
        if (!uri.Contains("://")) return uri;

        var parsed = new Uri(uri.Replace("jdbc:", ""));
        var builder = new MySqlConnectionStringBuilder
        {
            Server = parsed.Host,
            Port = parsed.IsDefaultPort || parsed.Port < 0 ? 3306u : (uint)parsed.Port,
            Database = parsed.AbsolutePath.Trim('/'),
        };

        var userInfo = parsed.UserInfo.Split(':', 2);
        if (userInfo[0].Length > 0) builder.UserID = Uri.UnescapeDataString(userInfo[0]);
        if (userInfo.Length > 1) builder.Password = Uri.UnescapeDataString(userInfo[1]);

        foreach (var pair in parsed.Query.TrimStart('?').Split('&', StringSplitOptions.RemoveEmptyEntries))
        {
            var parts = pair.Split('=', 2);
            var value = Uri.UnescapeDataString(parts.ElementAtOrDefault(1) ?? "");
            if (parts[0] == "user") builder.UserID = value;
            else if (parts[0] == "password") builder.Password = value;
        }

        return builder.ConnectionString;
    }

    private static MySqlCommand Command(MySqlConnection connection, string sql, object?[] args)
    {
        var command = new MySqlCommand(sql, connection);
        foreach (var arg in args) command.Parameters.Add(new MySqlParameter { Value = arg });
        return command;
    }

    private async Task<int> Execute(string sql, params object?[] args)
    {
        try
        {
            await using var connection = await pool.OpenConnectionAsync();
            await using var command = Command(connection, sql, args);
            return await command.ExecuteNonQueryAsync();
        }
        catch (MySqlException e)
        {
            Console.Error.WriteLine(e);
            return -1;
        }
    }

    private async Task<List<T>> Query<T>(string sql, Func<MySqlDataReader, T> map, params object?[] args)
    {
        var results = new List<T>();
        try
        {
            await using var connection = await pool.OpenConnectionAsync();
            await using var command = Command(connection, sql, args);
            await using var reader = await command.ExecuteReaderAsync();
            while (await reader.ReadAsync()) results.Add(map(reader));
        }
        catch (MySqlException e)
        {
            Console.Error.WriteLine(e);
        }
        return results;
    }

    private async Task<T> One<T>(string sql, Func<MySqlDataReader, T> map, T fallback, params object?[] args)
    {
        var results = await Query(sql, reader => reader.IsDBNull(0) ? fallback : map(reader), args);
        return results.Count > 0 ? results[0] : fallback;
    }

    private static ulong Id(MySqlDataReader reader, int column) => (ulong)reader.GetInt64(column);

    // SUM() comes back as DECIMAL, so no GetInt64 for you
    private static long Long(MySqlDataReader reader, int column) => Convert.ToInt64(reader.GetValue(column));

    // guilds

    public async Task EnsureGuild(ulong guildId)
    {
        if (await Execute("INSERT IGNORE INTO GUILDS (id, chances) VALUES (?, 1)", guildId) >= 0)
            guildChances.TryAdd(guildId, 1); // default 1%
    }

    // -1 means we've never seen this guild
    public async Task<int> GetGuildChances(ulong guildId)
    {
        if (guildChances.TryGetValue(guildId, out var cached)) return cached;
        var chances = await One("SELECT chances FROM GUILDS WHERE id = ?", r => r.GetInt32(0), -1, guildId);
        if (chances != -1) guildChances[guildId] = chances;
        return chances;
    }

    public async Task<bool> SetGuildChances(ulong guildId, int chances)
    {
        if (await Execute("UPDATE GUILDS SET chances = ? WHERE id = ?", chances, guildId) <= 0) return false;
        guildChances[guildId] = chances;
        return true;
    }

    public async Task<string?> GetSlashVer(ulong guildId)
    {
        if (slashVers.TryGetValue(guildId, out var cached)) return cached;
        return slashVers[guildId] = await One<string?>("SELECT slash_ver FROM GUILDS WHERE id = ?", r => r.GetString(0), null, guildId);
    }

    public async Task SetSlashVer(ulong guildId, string version)
    {
        if (await Execute("UPDATE GUILDS SET slash_ver = ? WHERE id = ?", version, guildId) >= 0)
            slashVers[guildId] = version;
    }

    // channels

    public async Task EnsureChannel(ulong channelId, ulong guildId)
    {
        if (knownChannels.ContainsKey(channelId)) return;
        if (await Execute("INSERT IGNORE INTO CHANNEL (id, guild) VALUES (?, ?)", channelId, guildId) >= 0)
            knownChannels.TryAdd(channelId, 0);
    }

    // -1 means no override, use the guild default
    public async Task<int> GetChannelChances(ulong channelId)
    {
        if (channelChances.TryGetValue(channelId, out var cached)) return cached;
        return channelChances[channelId] = await One("SELECT chances FROM CHANNEL WHERE id = ?", r => r.GetInt32(0), -1, channelId);
    }

    public async Task<bool> SetChannelChances(ulong channelId, int chances)
    {
        if (await Execute("UPDATE CHANNEL SET chances = ? WHERE id = ?", chances, channelId) <= 0) return false;
        channelChances[channelId] = chances;
        return true;
    }

    // users

    public async Task EnsureUser(ulong userId)
    {
        if (knownUsers.ContainsKey(userId)) return;
        if (await Execute("INSERT IGNORE INTO USERS (id) VALUES (?)", userId) >= 0)
            knownUsers.TryAdd(userId, 0);
    }

    public async Task<bool> IsBlacklisted(ulong userId)
    {
        if (blacklistedUsers.TryGetValue(userId, out var cached)) return cached;
        return blacklistedUsers[userId] = await One("SELECT blacklisted FROM USERS WHERE id = ?", r => r.GetBoolean(0), false, userId);
    }

    public async Task<bool> SetBlacklisted(ulong userId, bool blacklisted)
    {
        if (await Execute("UPDATE USERS SET blacklisted = ? WHERE id = ?", blacklisted, userId) <= 0) return false;
        blacklistedUsers[userId] = blacklisted;
        return true;
    }

    // true only the first time, so the meanie message doesn't turn into spam
    public async Task<bool> MarkBlockWarned(ulong userId)
    {
        if (warnedUsers.ContainsKey(userId)) return false;
        await EnsureUser(userId);

        // don't poison the cache if the migration hasn't been run yet
        if (await One("SELECT block_warned FROM USERS WHERE id = ?", r => r.GetBoolean(0), false, userId))
        {
            warnedUsers.TryAdd(userId, 0);
            return false;
        }

        if (await Execute("UPDATE USERS SET block_warned = TRUE WHERE id = ? AND block_warned = FALSE", userId) != 1)
            return false;

        warnedUsers.TryAdd(userId, 0);
        return true;
    }

    public async Task<bool> GetOptout(ulong userId, string column)
    {
        if (!OptoutColumns.Contains(column)) return false;
        if (optouts.TryGetValue((userId, column), out var cached)) return cached;
        return optouts[(userId, column)] = await One($"SELECT {column} FROM OPTOUTS WHERE id = ?", r => r.GetBoolean(0), false, userId);
    }

    public async Task<bool> SetOptout(ulong userId, string column, bool value)
    {
        if (!OptoutColumns.Contains(column)) return false;
        await EnsureUser(userId);
        if (await Execute("INSERT IGNORE INTO OPTOUTS (id) VALUES (?)", userId) < 0) return false;
        if (await Execute($"UPDATE OPTOUTS SET {column} = ? WHERE id = ?", value, userId) <= 0) return false;
        optouts[(userId, column)] = value;
        return true;
    }

    // emojis

    public async Task EnsureEmoji(ulong emojiId, ulong guildId)
    {
        if (knownEmojis.ContainsKey(emojiId)) return;
        if (await Execute("INSERT IGNORE INTO EMOJI (id, guild) VALUES (?, ?)", emojiId, guildId) >= 0)
            knownEmojis.TryAdd(emojiId, 0);
    }

    public async Task<bool> IsEmojiBlacklisted(ulong emojiId)
    {
        if (blacklistedEmojis.TryGetValue(emojiId, out var cached)) return cached;
        return blacklistedEmojis[emojiId] = await One("SELECT blacklisted FROM EMOJI WHERE id = ?", r => r.GetBoolean(0), false, emojiId);
    }

    public async Task<IReadOnlySet<ulong>> GetBlacklistedEmojiIds(ulong guildId)
    {
        if (guildBlacklistedEmojis.TryGetValue(guildId, out var cached)) return cached;
        var ids = await Query("SELECT id FROM EMOJI WHERE guild = ? AND blacklisted = TRUE", r => Id(r, 0), guildId);
        return guildBlacklistedEmojis[guildId] = [.. ids];
    }

    public async Task<bool> SetEmojiBlacklisted(ulong emojiId, bool blacklisted)
    {
        if (await Execute("UPDATE EMOJI SET blacklisted = ? WHERE id = ?", blacklisted, emojiId) <= 0) return false;
        blacklistedEmojis[emojiId] = blacklisted;
        guildBlacklistedEmojis.Clear(); // rare enough to just nuke it
        return true;
    }

    public Task<List<CountEntry>> GetEmojiLeaderboard(ulong guildId, int limit) =>
        Query("SELECT id, times_used FROM EMOJI WHERE guild = ? AND blacklisted = FALSE ORDER BY times_used DESC LIMIT ?",
            r => new CountEntry(Id(r, 0), Long(r, 1)), guildId, limit);

    public Task<long> GetEmojiUsageTotal(ulong guildId) =>
        One("SELECT SUM(times_used) FROM EMOJI WHERE guild = ? AND blacklisted = FALSE", r => Long(r, 0), 0L, guildId);

    // reactions

    // everything a bot reaction touches, in one connection and one transaction. returns the new count
    public async Task<int> TrackReaction(ulong guildId, ulong channelId, ulong messageId, ulong emojiId, ulong? userId)
    {
        await EnsureChannel(channelId, guildId);
        await EnsureEmoji(emojiId, guildId);
        if (userId is { } id) await EnsureUser(id);

        try
        {
            await using var connection = await pool.OpenConnectionAsync();
            await using var transaction = await connection.BeginTransactionAsync();

            async Task Run(string sql, params object?[] args)
            {
                await using var command = Command(connection, sql, args);
                command.Transaction = transaction;
                await command.ExecuteNonQueryAsync();
            }

            await Run("UPDATE EMOJI SET times_used = times_used + 1 WHERE id = ?", emojiId);
            await Run("INSERT IGNORE INTO MESSAGES (id, channel) VALUES (?, ?)", messageId, channelId);
            await Run("INSERT INTO REACTION (message, count, emoji) VALUES (?, 1, ?) ON DUPLICATE KEY UPDATE count = count + 1", messageId, emojiId);
            if (userId is { } user)
            {
                await Run("INSERT INTO USER_REACTIONS (user_id, guild_id, total) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE total = total + 1", user, guildId);
                await Run("INSERT INTO USER_EMOJI_STATS (user_id, guild_id, emoji_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE count = count + 1", user, guildId, emojiId);
            }

            await using var count = Command(connection, "SELECT count FROM REACTION WHERE message = ?", [messageId]);
            count.Transaction = transaction;
            var result = Convert.ToInt32(await count.ExecuteScalarAsync());

            await transaction.CommitAsync();
            return result;
        }
        catch (MySqlException e)
        {
            Console.Error.WriteLine(e);
            return 0;
        }
    }

    // what humans react with, per channel and per author. this is the "smart" part
    public async Task LearnReaction(ulong guildId, ulong channelId, ulong? authorId, ulong emojiId)
    {
        await EnsureChannel(channelId, guildId);
        await EnsureEmoji(emojiId, guildId);
        await Execute("INSERT INTO CHANNEL_EMOJI_STATS (channel_id, emoji_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE count = count + 1", channelId, emojiId);
        if (authorId is { } author)
        {
            await EnsureUser(author);
            await Execute("INSERT INTO AUTHOR_EMOJI_STATS (user_id, emoji_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE count = count + 1", author, emojiId);
        }
    }

    public async Task<Dictionary<ulong, long>> GetLearnedWeights(ulong channelId, ulong authorId)
    {
        var rows = await Query("""
            SELECT emoji_id, SUM(count) FROM (
                SELECT emoji_id, count FROM CHANNEL_EMOJI_STATS WHERE channel_id = ?
                UNION ALL
                SELECT emoji_id, count FROM AUTHOR_EMOJI_STATS WHERE user_id = ?
            ) learned GROUP BY emoji_id
            """, r => new CountEntry(Id(r, 0), Long(r, 1)), channelId, authorId);
        return rows.ToDictionary(row => row.Id, row => row.Count);
    }

    // a human joined in on one of our reactions. returns the new count
    public async Task<int> UpsertReaction(ulong messageId, ulong emojiId)
    {
        await Execute("INSERT INTO REACTION (message, count, emoji) VALUES (?, 1, ?) ON DUPLICATE KEY UPDATE count = count + 1", messageId, emojiId);
        return await One("SELECT count FROM REACTION WHERE message = ?", r => r.GetInt32(0), 0, messageId);
    }

    public Task<ulong?> GetReactionEmoji(ulong messageId) =>
        One<ulong?>("SELECT emoji FROM REACTION WHERE message = ?", r => Id(r, 0), null, messageId);

    public Task<List<ReactLeaderboardEntry>> GetReactLeaderboard(ulong guildId, int limit) =>
        Query("SELECT r.message, m.channel, r.count, r.emoji FROM REACTION r " +
              "JOIN MESSAGES m ON r.message = m.id " +
              "JOIN CHANNEL c ON m.channel = c.id " +
              "WHERE c.guild = ? ORDER BY r.count DESC LIMIT ?",
            r => new ReactLeaderboardEntry(Id(r, 0), Id(r, 1), r.GetInt32(2), Id(r, 3)), guildId, limit);

    public Task<long> GetReactionGrandTotal(ulong guildId) =>
        One("SELECT SUM(r.count) FROM REACTION r " +
            "JOIN MESSAGES m ON r.message = m.id " +
            "JOIN CHANNEL c ON m.channel = c.id WHERE c.guild = ?",
            r => Long(r, 0), 0L, guildId);

    // reactboard

    public async Task<ReactboardEntry?> GetReactboardForGuild(ulong guildId)
    {
        if (reactboards.TryGetValue(guildId, out var cached)) return cached;
        return reactboards[guildId] = await One<ReactboardEntry?>(
            "SELECT r.channel, r.threshold FROM REACTBOARD r JOIN CHANNEL c ON r.channel = c.id WHERE c.guild = ? LIMIT 1",
            r => new ReactboardEntry(Id(r, 0), r.GetInt32(1)), null, guildId);
    }

    public async Task SetReactboard(ulong channelId, int threshold)
    {
        await Execute("INSERT INTO REACTBOARD (channel, threshold) VALUES (?, ?) ON DUPLICATE KEY UPDATE threshold = VALUES(threshold)", channelId, threshold);
        reactboards.Clear();
    }

    public async Task RemoveReactboard(ulong channelId)
    {
        await Execute("DELETE FROM REACTBOARD WHERE channel = ?", channelId);
        reactboards.Clear();
    }

    public Task<ulong?> GetReactboardMessage(ulong messageId) =>
        One<ulong?>("SELECT reactboard_message FROM MESSAGES WHERE id = ?", r => Id(r, 0), null, messageId);

    // 1 = claimed, 0 = another reaction got there first, -1 = database error
    public Task<int> ClaimReactboardMessage(ulong messageId, ulong reactboardMessageId) =>
        Execute("UPDATE MESSAGES SET reactboard_message = ? WHERE id = ? AND reactboard_message IS NULL",
            reactboardMessageId, messageId);

    public async Task ClearReactboardMessage(ulong messageId) =>
        await Execute("UPDATE MESSAGES SET reactboard_message = NULL WHERE id = ?", messageId);

    // user stats

    public Task<List<CountEntry>> GetUserReactionLeaderboard(ulong guildId, int limit) =>
        Query("SELECT user_id, total FROM USER_REACTIONS WHERE guild_id = ? ORDER BY total DESC LIMIT ?",
            r => new CountEntry(Id(r, 0), Long(r, 1)), guildId, limit);

    public Task<long> GetUserReactionTotal(ulong userId, ulong guildId) =>
        One("SELECT total FROM USER_REACTIONS WHERE user_id = ? AND guild_id = ?", r => Long(r, 0), 0L, userId, guildId);

    public Task<List<CountEntry>> GetUserEmojiStats(ulong userId, ulong guildId, int limit) =>
        Query("SELECT emoji_id, count FROM USER_EMOJI_STATS WHERE user_id = ? AND guild_id = ? ORDER BY count DESC LIMIT ?",
            r => new CountEntry(Id(r, 0), Long(r, 1)), userId, guildId, limit);

    public Task<long> GetUserReactionGrandTotal(ulong guildId) =>
        One("SELECT SUM(total) FROM USER_REACTIONS WHERE guild_id = ?", r => Long(r, 0), 0L, guildId);

    public ValueTask DisposeAsync() => pool.DisposeAsync();
}
