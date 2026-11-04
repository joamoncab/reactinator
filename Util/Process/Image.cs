using NetCord;
using Reactinator.Util.Get;
using SkiaSharp;

namespace Reactinator.Util.Process;

public static class Image
{
    public static string AvatarUrl(User user, int size)
    {
        // prefer the guild avatar (like discord's "effective avatar"), then the global one
        var url = user is GuildUser guildUser && guildUser.GetGuildAvatarUrl(ImageFormat.Png) is { } guildAvatar
            ? guildAvatar
            : user.HasAvatar ? user.GetAvatarUrl(ImageFormat.Png)! : user.DefaultAvatarUrl;
        return url.SupportsSize ? url.ToString(size) : url.ToString();
    }

    public static async Task<SKBitmap?> Download(string url)
    {
        var bytes = await Http.Bytes(url);
        if (bytes == null) return null;

        try
        {
            return SKBitmap.Decode(bytes);
        }
        catch (Exception e)
        {
            Console.Error.WriteLine($"decoding {url} failed: {e.Message}");
            return null;
        }
    }

    public static void DrawCircular(SKCanvas canvas, SKBitmap image, SKRect target)
    {
        // crop the centered square so non-square images don't get squished
        var side = Math.Min(image.Width, image.Height);
        var source = SKRect.Create((image.Width - side) / 2f, (image.Height - side) / 2f, side, side);

        canvas.Save();
        canvas.ClipRoundRect(new SKRoundRect(target, target.Width / 2), antialias: true);
        using var snapshot = SKImage.FromBitmap(image);
        canvas.DrawImage(snapshot, source, target, new SKSamplingOptions(SKCubicResampler.Mitchell));
        canvas.Restore();
    }

    public static byte[] Encode(SKSurface surface)
    {
        using var snapshot = surface.Snapshot();
        using var png = snapshot.Encode(SKEncodedImageFormat.Png, 100);
        return png.ToArray();
    }
}
