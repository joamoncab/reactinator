using NetCord;
using NetCord.Gateway;
using NetCord.Rest;
using Reactinator.Util.Get;

namespace Reactinator.Commands.Meta;

public interface IBotCommand
{
    Task Execute(CommandContext context);
}

public sealed class CommandContext(SlashCommandInteraction interaction, RestClient client, Database database, Config config)
{
    public const string NotAuthorized = "you are not authorized to use this command.";

    private bool deferred;

    public SlashCommandInteraction Interaction => interaction;
    public Database Database => database;
    public Config Config => config;
    public User User => interaction.User;
    public Guild? Guild => interaction.Guild;
    public ulong GuildId => interaction.GuildId ?? 0;
    public RestClient Rest => client;

    public bool IsOwner => User.Id.ToString() == config.AuthorizedUser;
    public bool IsMod => IsOwner || (User is GuildInteractionUser member && member.Permissions.HasFlag(Permissions.ManageMessages));

    // "thinking..." so discord doesn't give up on us after 3 seconds
    public async Task Defer(bool ephemeral = false)
    {
        await interaction.SendResponseAsync(InteractionCallback.DeferredMessage(ephemeral ? MessageFlags.Ephemeral : null));
        deferred = true;
    }

    public Task Respond(string content, bool ephemeral = false) =>
        Respond(new InteractionMessageProperties { Content = content }, ephemeral);

    public async Task Respond(InteractionMessageProperties message, bool ephemeral = false)
    {
        if (ephemeral) message.Flags = MessageFlags.Ephemeral;
        if (deferred) await interaction.SendFollowupMessageAsync(message);
        else await interaction.SendResponseAsync(InteractionCallback.Message(message));
    }

    public Task RespondError(string content) => Respond(content + Util.Send.Messages.BlameMud, ephemeral: true);

    private ApplicationCommandInteractionDataOption? Option(string name) =>
        interaction.Data.Options.FirstOrDefault(option => option.Name == name);

    public string? String(string name) => Option(name)?.Value;

    public long? Integer(string name) => long.TryParse(Option(name)?.Value, out var value) ? value : null;

    public User? UserOption(string name) =>
        ulong.TryParse(Option(name)?.Value, out var id) && interaction.Data.ResolvedData?.Users?.TryGetValue(id, out var user) == true ? user : null;

    public Channel? ChannelOption(string name) =>
        ulong.TryParse(Option(name)?.Value, out var id) && interaction.Data.ResolvedData?.Channels?.TryGetValue(id, out var channel) == true ? channel : null;
}
