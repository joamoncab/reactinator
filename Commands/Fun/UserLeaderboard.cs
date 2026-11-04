using System.Text;
using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Fun;

public class UserLeaderboard : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer();
        var entries = await context.Database.GetUserReactionLeaderboard(context.GuildId, 10);
        if (entries.Count == 0)
        {
            await context.Respond("no reaction data yet.");
            return;
        }

        // fetch everyone at once instead of one by one like a caveman
        var names = await Task.WhenAll(entries.Select(entry => Leaderboards.Name(context, entry.Id)));

        var sb = new StringBuilder("**most reacted users**\n");
        for (var i = 0; i < entries.Count; i++)
            sb.Append($"{i + 1}. {names[i]} — **{entries[i].Count}** reactions\n");
        sb.Append($"total: **{await context.Database.GetUserReactionGrandTotal(context.GuildId)}** reactions");

        await context.Respond(sb.ToString());
    }
}
