using System.Text;
using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Fun;

public class EmojiLeaderboard : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer();
        var entries = await context.Database.GetEmojiLeaderboard(context.GuildId, 10);
        if (entries.Count == 0)
        {
            await context.Respond("no emoji usage data yet.");
            return;
        }

        var sb = new StringBuilder("**emoji leaderboard**\n");
        foreach (var (entry, rank) in entries.Select((entry, i) => (entry, i + 1)))
            sb.Append($"{rank}. {Leaderboards.Emoji(context, entry.Id)} — used **{entry.Count}** times\n");
        sb.Append($"total: **{await context.Database.GetEmojiUsageTotal(context.GuildId)}** reactions");

        await context.Respond(sb.ToString());
    }
}
