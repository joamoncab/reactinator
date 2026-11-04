using NetCord;
using NetCord.Rest;
using Reactinator.Commands.Meta;
using Reactinator.Util.Get;

namespace Reactinator.Commands;

public static class SlashDispatcher
{
    public static async Task Use(SlashCommandInteraction interaction, RestClient client, Database database, Config config)
    {
        var context = new CommandContext(interaction, client, database, config);
        if (!CommandsList.Commands.TryGetValue(interaction.Data.Name, out var command))
        {
            await context.Respond("how did you do this?", ephemeral: true);
            return;
        }
        await command.Command.Execute(context);
    }
}
