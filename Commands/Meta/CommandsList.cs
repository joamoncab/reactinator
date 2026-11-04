using NetCord;
using NetCord.Rest;
using Reactinator.Commands.Chances;
using Reactinator.Commands.Fun;
using Reactinator.Commands.Settings;
using OptionType = NetCord.ApplicationCommandOptionType;

namespace Reactinator.Commands.Meta;

public record CommandObject(
    string Description,
    IBotCommand Command,
    bool HiddenFromHelp = false,
    bool UserInstallable = false, // whether to register as a user-installable app
    params ApplicationCommandOptionProperties[] Options);

public static class CommandsList
{
    /*
     * command list: this stores all info related to commands. since it points at actual classes, it cannot live on the database (nor a json file)
     */
    public static readonly IReadOnlyDictionary<string, CommandObject> Commands = new Dictionary<string, CommandObject>
    {
        ["chances"] = new("get the current guild or channel reaction chances", new GetChance(),
            Options: [Option(OptionType.Channel, "channel", "channel to get chances for", false)]),
        ["setchance"] = new("set the current guild reaction chances", new SetChance(),
            Options: [Option(OptionType.Integer, "chance", "chance (%) of reacting to a message")]),
        ["setchancechannel"] = new("set per-channel reaction chances (overrides guild default)", new SetChanceChannel(),
            Options: [
                Option(OptionType.Integer, "chance", "chance (%) of reacting in this channel"),
                Option(OptionType.Channel, "channel", "channel to set chances for (will take actual channel if none provided)", false),
            ]),
        ["mpreg"] = new("put someone's pfp on discord's mpreg emoji", new Mpreg(), UserInstallable: true,
            Options: [Option(OptionType.Mentionable, "user", "user mention to mpreg")]),
        ["optout"] = new("toggle opt-out for bot features", new OptOut(),
            Options: [Option(OptionType.String, "type", "what to opt out of")
                .WithChoices(Util.Get.Database.OptoutColumns.Select(column => new ApplicationCommandOptionChoiceProperties(column, column)))]),
        ["blacklistemoji"] = new("toggle blacklist on a custom emoji", new BlacklistEmoji(),
            Options: [Option(OptionType.String, "emoji", "the custom emoji to blacklist")]),
        ["reactboard"] = new("configure the reactboard channel and threshold", new SetReactboard(),
            Options: [
                Option(OptionType.Channel, "channel", "channel to post reactboard messages in"),
                Option(OptionType.Integer, "threshold", "reaction count threshold (0 to disable reactboard)"),
            ]),
        ["blacklist"] = new("toggle blacklist on a user (bot staff only)", new Blacklist(), HiddenFromHelp: true,
            Options: [Option(OptionType.User, "user", "user to blacklist")]),
        ["emojileaderboard"] = new("show the top emojis by usage in this server", new EmojiLeaderboard()),
        ["soundboard"] = new("play a soundboard effect", new Soundboard(), UserInstallable: true,
            Options: [Option(OptionType.String, "sound", "the soundboard effect or query to play")]),
        ["copypasta"] = new("send a copypasta", new Copypasta(), UserInstallable: true,
            Options: [Option(OptionType.String, "copypasta", "the copypasta to send")]),
        ["reactleaderboard"] = new("show the top reacted messages in this server", new ReactLeaderboard()),
        ["refreshcommands"] = new("refresh the slash commands for this guild (bot staff only)", new RefreshCommands(), HiddenFromHelp: true),
        ["userleaderboard"] = new("show the most reacted users in this server", new UserLeaderboard()),
        ["userstats"] = new("view reaction stats for a user", new UserStats(),
            Options: [Option(OptionType.User, "user", "the user to check stats for", false)]),
        ["help"] = new("show a list of all available commands", new Help(), HiddenFromHelp: true),
    };

    // all my homies love one-liners
    private static ApplicationCommandOptionProperties Option(OptionType type, string name, string description, bool required = true) =>
        new(type, name, description) { Required = required };

    public static IEnumerable<SlashCommandProperties> GuildCommands() =>
        Commands.Where(entry => !entry.Value.UserInstallable).Select(entry => ToProperties(entry.Key, entry.Value));

    // user-installable commands are global and work everywhere
    public static IEnumerable<SlashCommandProperties> GlobalCommands() =>
        Commands.Where(entry => entry.Value.UserInstallable).Select(entry =>
        {
            var properties = ToProperties(entry.Key, entry.Value);
            properties.IntegrationTypes = [ApplicationIntegrationType.UserInstall, ApplicationIntegrationType.GuildInstall];
            properties.Contexts = [InteractionContextType.Guild, InteractionContextType.BotDMChannel, InteractionContextType.DMChannel];
            return properties;
        });

    private static SlashCommandProperties ToProperties(string name, CommandObject command) =>
        new(name, command.Description) { Options = command.Options };
}
