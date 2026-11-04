namespace Reactinator.Util.Get;

public static class Assets
{
    // null when someone lost the asset. a copy next to the binary wins, so replies can be edited without rebuilding
    public static byte[]? Read(string name)
    {
        var local = Path.Combine(AppContext.BaseDirectory, name);
        if (File.Exists(local)) return File.ReadAllBytes(local);

        using var stream = typeof(Assets).Assembly.GetManifestResourceStream(name);
        if (stream == null) return null;
        using var memory = new MemoryStream();
        stream.CopyTo(memory);
        return memory.ToArray();
    }
}
