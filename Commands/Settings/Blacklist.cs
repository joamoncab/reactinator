using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Settings;

public class Blacklist : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer(ephemeral: true);
        if (!context.IsOwner)
        {
            await context.Respond(CommandContext.NotAuthorized, ephemeral: true);
            return;
        }
        if (context.UserOption("user") is not { } user)
        {
            await context.Respond("who? that user doesn't exist.", ephemeral: true);
            return;
        }

        await context.Database.EnsureUser(user.Id);
        var current = await context.Database.IsBlacklisted(user.Id);
        await context.Database.SetBlacklisted(user.Id, !current);
        await context.Respond((current ? "un" : "") + "blacklisted user.", ephemeral: true);
    }
}
