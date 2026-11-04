using System.Text;
using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Fun;

public class ReactLeaderboard : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer();
        var entries = await context.Database.GetReactLeaderboard(context.GuildId, 10);
        if (entries.Count == 0)
        {
            await context.Respond("no message reaction data yet. yet...");
            return;
        }

        var sb = new StringBuilder("**reactboard leaderboard**\n");
        foreach (var (entry, rank) in entries.Select((entry, i) => (entry, i + 1)))
            sb.Append($"{rank}. {Leaderboards.Emoji(context, entry.EmojiId)} **{entry.Count}** | https://discord.com/channels/{context.GuildId}/{entry.ChannelId}/{entry.MessageId}\n");
        sb.Append($"total: **{await context.Database.GetReactionGrandTotal(context.GuildId)}** overall reactions");

        await context.Respond(sb.ToString());
    }
}
