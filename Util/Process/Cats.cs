using NetCord;

namespace Reactinator.Util.Process;

public static class Cats
{
    // do we even need a whole class for cats
    public static bool IsCatChannel(string? channelName)
    {
        var name = channelName?.ToLowerInvariant() ?? "";
        return name.Contains("-cat") || name.Contains("cat-") || name.Contains("cats-");
    }

    public static IReadOnlyCollection<GuildEmoji> GetCatEmojis(IEnumerable<GuildEmoji> emojis)
    {
        var all = emojis.ToList();
        var cats = all.Where(emoji => emoji.Name.Contains("cat", StringComparison.OrdinalIgnoreCase)).ToList();
        return cats.Count > 0 ? cats : all;
    }
}
