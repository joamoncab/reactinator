using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Chances;

public class SetChance : IBotCommand
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

        var updated = await context.Database.SetGuildChances(context.GuildId, (int)chance);
        await context.Respond(updated ? $"chances updated to {chance}%" : "failed to update chances.", ephemeral: true);
    }
}
