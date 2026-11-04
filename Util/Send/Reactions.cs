using NetCord;
using NetCord.Gateway;
using NetCord.Rest;
using Reactinator.Util.Get;
using Reactinator.Util.Process;

namespace Reactinator.Util.Send;

public static class Reactions
{
    private const int ReactionBlocked = 90001;
    private const int UnknownMessage = 10008;

    public static async Task React(Message message, IEnumerable<GuildEmoji>? emojis, Database database, bool byLuck)
    {
        if (message.Guild is not { } guild) return;

        var blacklisted = await database.GetBlacklistedEmojiIds(guild.Id);
        var available = (emojis ?? guild.Emojis.Values)
            .Where(emoji => emoji.Available != false) // we don't care if it's blacklisted if there are not enough boosts to use it
            .Where(emoji => !blacklisted.Contains(emoji.Id))
            .ToList();
        if (available.Count == 0) return;

        var emoji = Pick(available, await database.GetLearnedWeights(message.ChannelId, message.Author.Id));

        try
        {
            await message.AddReactionAsync(new ReactionEmojiProperties(emoji.Name, emoji.Id));
        }
        catch (RestException e) when (e.Error?.Code == ReactionBlocked)
        {
            if (await database.MarkBlockWarned(message.Author.Id))
                await message.Channel!.SendMessageAsync($"<@{message.Author.Id}> is a meanie and has blocked me :(\n" +
                    "-# no need to block me btw, `/optout react` makes me leave you alone. there's also `mpreg`");
            return;
        }

        // only track user stats for reactions triggered by luck (not mentions)
        var count = await database.TrackReaction(guild.Id, message.ChannelId, message.Id, emoji.Id, byLuck ? message.Author.Id : null);
        await PostToReactboard(guild, database, emoji.ToString(), count, message.ChannelId, message.Id);
    }

    // emojis people actually use here win more often, but every emoji keeps a chance. chaos must survive
    private static GuildEmoji Pick(List<GuildEmoji> emojis, Dictionary<ulong, long> learned)
    {
        var weights = emojis.Select(emoji => 1 + Math.Sqrt(learned.GetValueOrDefault(emoji.Id))).ToList();
        var roll = Random.Shared.NextDouble() * weights.Sum();
        for (var i = 0; i < emojis.Count; i++)
            if ((roll -= weights[i]) < 0) return emojis[i];
        return emojis[^1];
    }

    public static Task ReactWithCats(Message message, Database database, bool byLuck) =>
        React(message, Cats.GetCatEmojis(message.Guild?.Emojis.Values ?? []), database, byLuck);

    public static async Task PostToReactboard(Guild guild, Database database, string emoji, int count, ulong channelId, ulong messageId)
    {
        var reactboard = await database.GetReactboardForGuild(guild.Id);
        if (reactboard == null || count < reactboard.Threshold) return;
        if (!guild.Channels.TryGetValue(reactboard.Channel, out var channel) || channel is not TextChannel target) return;

        var content = $"{emoji} **{count}** | https://discord.com/channels/{guild.Id}/{channelId}/{messageId}";

        // the board entry already exists, so just keep its counter fresh
        if (await database.GetReactboardMessage(messageId) is { } reactboardMessageId)
        {
            try
            {
                await target.ModifyMessageAsync(reactboardMessageId, options => options.Content = content);
                return;
            }
            catch (RestException e) when (e.Error?.Code == UnknownMessage)
            {
                // someone deleted the board entry, so let a new one take its place
                await database.ClearReactboardMessage(messageId);
            }
        }

        var sent = await target.SendMessageAsync(content);
        var claimed = await database.ClaimReactboardMessage(messageId, sent.Id);
        if (claimed != 0) return; // claimed it, or the database failed and we keep the post

        // two reactions arrived at once and the other one won the race, keep its message instead
        if (await database.GetReactboardMessage(messageId) is { } existing)
        {
            try
            {
                await target.ModifyMessageAsync(existing, options => options.Content = content);
            }
            catch (RestException e) when (e.Error?.Code == UnknownMessage)
            {
                // the winner got deleted too, so ours takes its place (you never hate race conditions enough)
                await database.ClearReactboardMessage(messageId);
                await database.ClaimReactboardMessage(messageId, sent.Id);
                return;
            }
            await target.DeleteMessageAsync(sent.Id);
        }
    }
}
