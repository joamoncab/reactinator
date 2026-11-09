using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Chances;

public class SetChanceChannel : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer(ephemeral: true);
        if (!context.IsMod)
        {
            await context.Respond(CommandContext.NotAuthorized, ephemeral: true);
            return;
        }

        var chance = context.Integer("chance") ?? -1;
        if (chance is < 0 or > 100)
        {
            await context.Respond("chance must be between 0 and 100.", ephemeral: true);
            return;
        }

        var channelId = context.ChannelOption("channel")?.Id ?? context.Interaction.Channel.Id;
        await context.Database.EnsureChannel(channelId, context.GuildId);

        var updated = await context.Database.SetChannelChances(channelId, (int)chance);
        await context.Respond(updated ? $"channel chances for <#{channelId}> updated to {chance}%" : "failed to update channel chances.", ephemeral: true);
    }
}
