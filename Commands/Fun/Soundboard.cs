using System.Net;
using NetCord.Rest;
using Reactinator.Commands.Meta;
using Reactinator.Util.Get;
using Reactinator.Util.Process;
using Reactinator.Util.Send;

namespace Reactinator.Commands.Fun;

public class Soundboard : IBotCommand
{
    public async Task Execute(CommandContext context)
    {
        if (context.Config.SoundsSource is not { } source)
        {
            await context.Respond("soundboard source is not configured. blame someone else, not me, ey?", ephemeral: true);
            return;
        }

        await context.Defer();
        var query = Text.StripMentions(context.String("sound") ?? "");

        // same silly rules as pinging me, they live in replies.json
        var url = source + WebUtility.UrlEncode(query);
        var (fileName, data) = TextDispatcher.SillyMedia(query) is { } asset
            ? (asset, Assets.Read(asset))
            : (Messages.SoundFileName(url), await Http.Bytes(url));

        if (data == null)
        {
            await context.RespondError("couldn't find that weird media you wanted");
            return;
        }
        await context.Respond(new InteractionMessageProperties { Attachments = [new AttachmentProperties(fileName, new MemoryStream(data))] });
    }
}
