using Reactinator.Commands.Meta;

namespace Reactinator.Commands.Settings;

public class OptOut : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer(ephemeral: true);
        var type = context.String("type") ?? "";

        var current = await context.Database.GetOptout(context.User.Id, type);
        if (!await context.Database.SetOptout(context.User.Id, type, !current))
        {
            await context.Respond("couldn't change that, the database is being weird. try again later.", ephemeral: true);
            return;
        }
        await context.Respond((current ? "re-enabled " : "opted out of ") + type + ".", ephemeral: true);
    }
}
