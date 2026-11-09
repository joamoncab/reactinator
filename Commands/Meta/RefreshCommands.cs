using NetCord.Rest;

namespace Reactinator.Commands.Meta;

public class RefreshCommands : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        await context.Defer(ephemeral: true);
        if (!context.IsOwner)
        {
            await context.Respond(CommandContext.NotAuthorized, ephemeral: true);
            return;
        }

        var applicationId = context.Interaction.ApplicationId;

        try
        {
            await context.Rest.BulkOverwriteGuildApplicationCommandsAsync(applicationId, context.GuildId, CommandsList.GuildCommands());
            await context.Respond("guild commands successfully refreshed!", ephemeral: true);
        }
        catch (RestException e)
        {
            await context.Respond("failed to refresh guild commands: " + e.Message, ephemeral: true);
        }

        try
        {
            await context.Rest.BulkOverwriteGlobalApplicationCommandsAsync(applicationId, CommandsList.GlobalCommands());
            await context.Respond("global user-installable commands successfully refreshed!", ephemeral: true);
        }
        catch (RestException e)
        {
            await context.Respond("failed to refresh global commands: " + e.Message, ephemeral: true);
        }
    }
}
