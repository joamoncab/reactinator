namespace Reactinator.Util.Get;

public static class Http
{
    private static readonly HttpClient client = new() { Timeout = TimeSpan.FromSeconds(15) };

    // null = not found (or the internet said no)
    public static async Task<byte[]?> Bytes(string url)
    {
        try
        {
            using var response = await client.GetAsync(url);
            return response.IsSuccessStatusCode ? await response.Content.ReadAsByteArrayAsync() : null;
        }
        catch (Exception e) when (e is HttpRequestException or TaskCanceledException or UriFormatException)
        {
            Console.Error.WriteLine($"fetching {url} failed: {e.Message}");
            return null;
        }
    }
}
