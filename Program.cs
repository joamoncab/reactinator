using System.Runtime.InteropServices;
using NetCord;
using NetCord.Gateway;
using Reactinator;
using Reactinator.Commands.Meta;
using Reactinator.Events;
using Reactinator.Util.Get;

var config = Config.FromEnvironment();
await using var database = new Database(config.DbUri);

var client = new GatewayClient(new BotToken(config.Token), new GatewayClientConfiguration
{
    // no MessageContent intent: discord still sends content for messages that mention us,
    // which is the only place we read text. everything else goes through slash commands.
    Intents = GatewayIntents.Guilds
              | GatewayIntents.GuildMessages
              | GatewayIntents.GuildEmojisAndStickers
              | GatewayIntents.GuildMessageReactions,
    // netcord starts with an empty cache, so without this we would see no guilds, emojis or channels at all
    CacheProvider = ConcurrentGatewayClientCacheProvider.Empty,
    Presence = new PresenceProperties(UserStatusType.Idle)
    {
        Activities = [new UserActivityProperties("Custom Status", UserActivityType.Custom) { State = "behold the reactinator!!!" }],
    },
});

var messages = new MessageHandler(client, database, config);
var reactions = new ReactionHandler(client, database);
var slashCommands = new SlashCommandsHandler(client, database, config);

// one handler blowing up shouldn't take the whole bot with it
client.MessageCreate += message => Safely(() => messages.OnMessage(message));
client.MessageReactionAdd += reaction => Safely(() => reactions.OnReactionAdd(reaction));
client.InteractionCreate += interaction => Safely(() => slashCommands.OnInteraction(interaction));

var globalCommandsSent = false;
client.Ready += ready => Safely(async () =>
{
    Console.WriteLine($"logged in as {ready.User.Username}, behold");
    if (globalCommandsSent) return;
    globalCommandsSent = true;
    // user-installable commands are global, so they get sent once per boot instead of per guild
    await client.Rest.BulkOverwriteGlobalApplicationCommandsAsync(ready.ApplicationId, CommandsList.GlobalCommands());
});

var stop = new TaskCompletionSource();
using var sigterm = PosixSignalRegistration.Create(PosixSignal.SIGTERM, signal => { signal.Cancel = true; stop.TrySetResult(); });
using var sigint = PosixSignalRegistration.Create(PosixSignal.SIGINT, signal => { signal.Cancel = true; stop.TrySetResult(); });

await client.StartAsync();
await stop.Task;
await client.CloseAsync();
Console.WriteLine("i always come back");

static async ValueTask Safely(Func<ValueTask> handler)
{
    try
    {
        await handler();
    }
    catch (Exception e)
    {
        Console.Error.WriteLine(e);
    }
}
