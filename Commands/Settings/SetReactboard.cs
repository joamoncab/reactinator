using NetCord;
using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Settings;

public class SetReactboard : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer(ephemeral: true);
        if (!context.IsMod)
        {
            await context.Respond(CommandContext.NotAuthorized, ephemeral: true);
            return;
        }

        if (context.ChannelOption("channel") is not TextChannel channel)
        {
            await context.Respond("the reactboard needs a text channel.", ephemeral: true);
            return;
        }

        var channelId = channel.Id;
        var threshold = context.Integer("threshold") ?? 0;

        if (threshold <= 0)
        {
            await context.Database.RemoveReactboard(channelId);
            await context.Respond("reactboard removed.", ephemeral: true);
            return;
        }

        await context.Database.EnsureChannel(channelId, context.GuildId);
        await context.Database.SetReactboard(channelId, (int)threshold);
        await context.Respond($"reactboard set to <#{channelId}> with threshold {threshold}.", ephemeral: true);
    }
}
