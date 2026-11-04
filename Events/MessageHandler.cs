using NetCord;
using NetCord.Gateway;
using Reactinator.Commands;
using Reactinator.Commands.Meta;
using Reactinator.Util.Get;
using Reactinator.Util.Process;
using Reactinator.Util.Send;

namespace Reactinator.Events;

public sealed class MessageHandler(GatewayClient client, Database database, Config config)
{
    public async ValueTask OnMessage(Message message)
    {
        if (message.GuildId is not { } guildId) return;

        var guildChance = await database.GetGuildChances(guildId);

        // no guild records, create all data
        if (guildChance == -1)
        {
            await database.EnsureGuild(guildId);
            if (config.SlashVersion != null) await database.SetSlashVer(guildId, config.SlashVersion);
            await UpdateGuildSlashCommands(guildId);
            return;
        }

        // update slash commands, if needed (discord is just like that idk)
        if (config.SlashVersion != null && await database.GetSlashVer(guildId) != config.SlashVersion)
        {
            await database.SetSlashVer(guildId, config.SlashVersion);
            await UpdateGuildSlashCommands(guildId);
        }

        // haha blacklisted
        if (await database.IsBlacklisted(message.Author.Id)) return;

        if (message.MentionedUsers.Any(user => user.Id == client.Id))
        {
            if (message.Author.Id == client.Id) return; // ignore self but not other bots (very important for the silly)
            if (message.ReferencedMessage is { } referenced && referenced.Author.Id != client.Id)
                await TextDispatcher.UseSilly(message, config.SoundsSource, database);
            else
                await TextDispatcher.Use(message, database);
        }

        if (await database.GetOptout(message.Author.Id, "react")) return;

        var channelChance = await database.GetChannelChances(message.ChannelId);
        var chance = channelChance >= 0 ? channelChance : guildChance;
        if (Random.Shared.Next(100) >= chance) return;

        // uses only cat emojis in cat channels (meow)
        if (Cats.IsCatChannel((message.Channel as TextGuildChannel)?.Name))
            await Reactions.ReactWithCats(message, database, byLuck: true);
        else
            await Reactions.React(message, null, database, byLuck: true);
    }

    private async Task UpdateGuildSlashCommands(ulong guildId) =>
        await client.Rest.BulkOverwriteGuildApplicationCommandsAsync(client.Id, guildId, CommandsList.GuildCommands());
}
