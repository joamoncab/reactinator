using System.Net;
using System.Text;
using Reactinator.Commands.Meta;
using Reactinator.Util.Get;
using Reactinator.Util.Process;
using Reactinator.Util.Send;

namespace Reactinator.Commands.Fun;

public class Copypasta : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        if (context.Config.CopypastaSource is not { } source)
        {
            await context.Respond("copypasta source is not configured. blame someone else, not me, ey?", ephemeral: true);
            return;
        }

        await context.Defer();
        var query = Text.StripMentions(context.String("copypasta") ?? "");

        if (await Http.Bytes(source + WebUtility.UrlEncode(query)) is not { } data)
        {
            await context.RespondError("couldn't find that weird copypasta you wanted");
            return;
        }
        await context.Respond(Messages.Truncate(Encoding.UTF8.GetString(data)));
    }
}
