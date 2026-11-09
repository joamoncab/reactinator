using System.Net;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;
using NetCord.Gateway;
using NetCord.Rest;
using Reactinator.Util.Get;
using Reactinator.Util.Process;
using Reactinator.Util.Send;

namespace Reactinator.Commands;

public static class TextDispatcher
{
    // react: "random", "cats" or a unicode emoji
    private record Rule(Regex Pattern, string[] Replies, string? React, string? Media);

    // first match wins, so order in replies.json matters
    private static readonly Dictionary<string, Rule[]> Rules = Load();

    public static async Task Use(Message message, Database database)
    {
        if (Match("mention", Text.StripMentions(message.Content)) is { } rule) await Run(message, rule, database);
        else await Reactions.React(message, null, database, byLuck: false);
    }

    // somebody replied to a message and pinged me with a sound query
    public static async Task UseSilly(Message message, string? soundSource, Database database)
    {
        var query = Text.StripMentions(message.Content);
        if (Match("silly", query) is { } rule)
        {
            await Run(message, rule, database);
            return;
        }
        if (soundSource == null || !await Messages.SendSoundFromUrl(message, soundSource + WebUtility.UrlEncode(query)))
            await Use(message, database);
    }

    // for /soundboard, same rules without the chatter
    public static string? SillyMedia(string text) => Match("silly", text)?.Media;

    private static Rule? Match(string key, string text) =>
        Rules[key].FirstOrDefault(rule => rule.Pattern.IsMatch(text));

    private static async Task Run(Message message, Rule rule, Database database)
    {
        if (rule.Replies.Length > 0) await Messages.Reply(message, rule.Replies[Random.Shared.Next(rule.Replies.Length)]);
        if (rule.Media != null) await Messages.SendAsset(message, rule.Media);

        await (rule.React switch
        {
            null => Task.CompletedTask,
            "random" => Reactions.React(message, null, database, byLuck: false),
            "cats" => Reactions.ReactWithCats(message, database, byLuck: false),
            var emoji => message.AddReactionAsync(new ReactionEmojiProperties(emoji)),
        });
    }

    private static Dictionary<string, Rule[]> Load()
    {
        var json = Assets.Read("replies.json") ?? throw new FileNotFoundException("replies.json went missing, i have nothing to say");
        var file = JsonSerializer.Deserialize(json, RepliesJson.Default.DictionaryStringRuleJsonArray)!;

        // \b so "hi" stops matching "this" and "ai" stops matching "said" (i'm sorry [not really])
        return file.ToDictionary(pair => pair.Key, pair => pair.Value.Select(rule => new Rule(
            new Regex($@"\b({string.Join('|', rule.Triggers)})\b", RegexOptions.IgnoreCase | RegexOptions.CultureInvariant),
            rule.Replies ?? [], rule.React, rule.Media)).ToArray());
    }
}

internal record RuleJson(string[] Triggers, string[]? Replies, string? React, string? Media);

// native aot hates reflection, so the json reader gets generated at compile time
[JsonSourceGenerationOptions(PropertyNameCaseInsensitive = true)]
[JsonSerializable(typeof(Dictionary<string, RuleJson[]>))]
internal partial class RepliesJson : JsonSerializerContext;
