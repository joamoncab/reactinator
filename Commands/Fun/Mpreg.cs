using NetCord.Rest;
using Reactinator.Commands.Meta;
using Reactinator.Util.Get;
using SkiaSharp;
using Image = Reactinator.Util.Process.Image;

namespace Reactinator.Commands.Fun;

public class Mpreg : IBotCommand
{
    private const int AvatarX = 253;
    private const int AvatarY = 15;
    private const int AvatarSize = 450;
    private const int AvatarDownloadSize = 512;

    public async Task Execute(CommandContext context)
    {
        if (context.UserOption("user") is not { } user)
        {
            await context.Respond("that's a role. roles can't get pregnant. yet.", ephemeral: true);
            return;
        }

        // respect mpreg opt-out
        if (await context.Database.GetOptout(user.Id, "mpreg"))
        {
            await context.Respond("that user has opted out of mpreg.", ephemeral: true);
            return;
        }

        await context.Defer();

        if (Assets.Read("mpreg.png") is not { } templateBytes)
        {
            await context.Respond("hey so i lost the mpreg"); // honestly too funny to not be seen
            return;
        }

        using var avatar = await Image.Download(Image.AvatarUrl(user, AvatarDownloadSize));
        if (avatar == null)
        {
            await context.Respond("uh oh something went wrong");
            return;
        }

        try
        {
            using var template = SKImage.FromEncodedData(templateBytes);
            using var surface = SKSurface.Create(new SKImageInfo(template.Width, template.Height));
            surface.Canvas.DrawImage(template, 0, 0, SKSamplingOptions.Default);
            Image.DrawCircular(surface.Canvas, avatar, SKRect.Create(AvatarX, AvatarY, AvatarSize, AvatarSize));

            await context.Respond(new InteractionMessageProperties
            {
                Attachments = [new AttachmentProperties("mpreg_result.png", new MemoryStream(Image.Encode(surface)))],
            });
        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e);
            await context.Respond("uh oh something went wrong");
        }
    }
}
