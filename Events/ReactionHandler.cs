using NetCord.Gateway;
using Reactinator.Util.Get;
using Reactinator.Util.Send;

namespace Reactinator.Events;

public sealed class ReactionHandler(GatewayClient client, Database database)
{
    public async ValueTask OnReactionAdd(MessageReactionAddEventArgs reaction)
    {
        if (reaction.GuildId is not { } guildId) return;

        // clanker got a whole table for itself
        if (reaction.UserId == client.Id) return;

        // ban is ban heh
        if (await database.IsBlacklisted(reaction.UserId)) return;

        if (reaction.Emoji.Id is not { } emojiId) return;
        if (await database.IsEmojiBlacklisted(emojiId)) return;

        client.Cache.Guilds.TryGetValue(guildId, out var guild);

        // learn from emojis of this server only, foreign ones are no use to us
        if (guild?.Emojis.ContainsKey(emojiId) == true)
        {
            ulong? author = reaction.MessageAuthorId is { } id && !await database.GetOptout(id, "react") ? id : null;
            await database.LearnReaction(guildId, reaction.ChannelId, author, emojiId);
        }

        // only count people joining in on the emoji reactinator picked
        if (await database.GetReactionEmoji(reaction.MessageId) != emojiId) return;

        var count = await database.UpsertReaction(reaction.MessageId, emojiId);
        if (guild != null)
        {
            var emoji = $"<{(reaction.Emoji.Animated ? "a" : "")}:{reaction.Emoji.Name}:{emojiId}>";
            await Reactions.PostToReactboard(guild, database, emoji, count, reaction.ChannelId, reaction.MessageId);
        }
    }
}
