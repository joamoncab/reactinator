using System.Text;
using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Fun;

public class UserStats : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer();
        var user = context.UserOption("user") ?? context.User;
        var name = $"**{user.Username}**";

        var total = await context.Database.GetUserReactionTotal(user.Id, context.GuildId);
        if (total == 0)
        {
            await context.Respond($"{name} has no reactions yet.");
            return;
        }

        var stats = await context.Database.GetUserEmojiStats(user.Id, context.GuildId, 10);
        var sb = new StringBuilder($"reaction stats for {name}\n");
        foreach (var (entry, rank) in stats.Select((entry, i) => (entry, i + 1)))
            sb.Append($"{rank}. {Leaderboards.Emoji(context, entry.Id)} — **{entry.Count}** times\n");
        sb.Append($"total: **{total}** reactions");

        await context.Respond(sb.ToString());
    }
}
