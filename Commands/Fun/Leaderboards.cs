using NetCord.Rest;
using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Fun;

// shared bits for the leaderboard-ish commands
internal static class Leaderboards
{
    public static string Emoji(CommandContext context, ulong id) =>
        context.Guild?.Emojis.TryGetValue(id, out var emoji) == true ? emoji.ToString() : $"unknown ({id})";

    public static async Task<string> Name(CommandContext context, ulong id)
    {
        try
        {
            return $"**{(await context.Rest.GetUserAsync(id)).Username}**";
        }
        catch (RestException e)
        {
            return $"{e.Error?.Message ?? "unknown user"} (<@{id}>)";
        }
    }
}
