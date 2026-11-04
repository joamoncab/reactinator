using NetCord;
using NetCord.Gateway;
using NetCord.Rest;
using Reactinator.Commands;
using Reactinator.Util.Get;

namespace Reactinator.Events;

public sealed class SlashCommandsHandler(GatewayClient client, Database database, Config config)
{
    public async ValueTask OnInteraction(Interaction interaction)
    {
        if (interaction is not SlashCommandInteraction slash) return;

        // once again haha banned
        if (await database.IsBlacklisted(slash.User.Id))
        {
            await slash.SendResponseAsync(InteractionCallback.Message(new InteractionMessageProperties
            {
                Content = "you are blacklisted from using this bot. what have you done?",
                Flags = MessageFlags.Ephemeral,
            }));
            return;
        }

        await SlashDispatcher.Use(slash, client.Rest, database, config);
    }
}
