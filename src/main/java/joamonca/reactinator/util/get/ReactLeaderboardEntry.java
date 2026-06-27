package joamonca.reactinator.util.get;

public record ReactLeaderboardEntry(
        long messageId,
        long channelId,
        int count,
        long emojiId
) {}
