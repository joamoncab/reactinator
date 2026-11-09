using System.Net;
using NetCord.Gateway;
using NetCord.Rest;
using Reactinator.Util.Get;

namespace Reactinator.Util.Send;

public static class Messages
{
    public const string BlameMud = "\n<@807170846497570848> do better!!!"; // blaming mud for this btw

    public static Task Reply(Message message, string content) =>
        message.ReplyAsync(new ReplyMessageProperties { Content = content });

    public static Task ReplyError(Message message, string content) => Reply(message, content + BlameMud);

    public static async Task SendMedia(Message message, string fileName, byte[] data)
    {
        var properties = new MessageProperties { Attachments = [new AttachmentProperties(fileName, new MemoryStream(data))] };
        if (message.ReferencedMessage is { } referenced)
            properties.MessageReference = MessageReferenceProperties.Reply(referenced.Id, failIfNotExists: false);
        await message.Channel!.SendMessageAsync(properties);
    }

    public static async Task SendAsset(Message message, string fileName)
    {
        if (Assets.Read(fileName) is { } data) await SendMedia(message, fileName, data);
        else Console.Error.WriteLine($"asset not found: {fileName}");
    }

    // false if the soundboard has no idea what you meant
    public static async Task<bool> SendSoundFromUrl(Message message, string url)
    {
        if (await Http.Bytes(url) is not { } data) return false;
        await SendMedia(message, SoundFileName(url), data);
        return true;
    }

    public static string SoundFileName(string url)
    {
        var fileName = url[(url.LastIndexOf('/') + 1)..];
        return fileName.Contains(".mp3") ? fileName : WebUtility.UrlDecode(fileName) + ".mp3";
    }

    // discord says 2000 chars max, discord gets 2000 chars max (reactinator doesn't have money for nitro)
    public static string Truncate(string content) => content.Length > 2000 ? content[..1997] + "..." : content;
}
