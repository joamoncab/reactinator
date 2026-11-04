using System.Text.RegularExpressions;
using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Settings;

public partial class BlacklistEmoji : IBotCommand
{
    [GeneratedRegex(@"<a?:\w+:(\d+)>")]
    private static partial Regex EmojiPattern();

    public async Task Execute(CommandContext context)
    {
        await context.Defer(ephemeral: true);
        if (!context.IsMod)
        {
            await context.Respond(CommandContext.NotAuthorized, ephemeral: true);
            return;
        }

        var match = EmojiPattern().Match(context.String("emoji") ?? "");
        if (!match.Success)
        {
            await context.Respond("invalid emoji format. use a custom server emoji.", ephemeral: true);
            return;
        }

        var emojiId = ulong.Parse(match.Groups[1].Value);
        await context.Database.EnsureEmoji(emojiId, context.GuildId);
        var current = await context.Database.IsEmojiBlacklisted(emojiId);
        await context.Database.SetEmojiBlacklisted(emojiId, !current);
        await context.Respond((current ? "un" : "") + "blacklisted emoji.", ephemeral: true);
    }
}
