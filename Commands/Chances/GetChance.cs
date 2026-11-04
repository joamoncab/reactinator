using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Chances;

public class GetChance : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer(ephemeral: true);
        var channelId = context.ChannelOption("channel")?.Id ?? context.Interaction.Channel.Id;

        var channelChance = await context.Database.GetChannelChances(channelId);
        var guildChance = await context.Database.GetGuildChances(context.GuildId);

        if (channelChance >= 0)
            await context.Respond($"current chance in <#{channelId}> is {channelChance}%", ephemeral: true);
        else if (guildChance >= 0)
            await context.Respond($"no override set for <#{channelId}>. it uses the guild default ({guildChance}%).", ephemeral: true);
        else
            await context.Respond("failed to retrieve chances.", ephemeral: true);
    }
}
