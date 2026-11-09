using System.Text.RegularExpressions;

namespace Reactinator.Util.Process;

public static partial class Text
{
    [GeneratedRegex(@"<@[!&]?\d+>|<#\d+>")]
    private static partial Regex Mentions();

    public static string StripMentions(string raw) => Mentions().Replace(raw, "").Trim();
}
