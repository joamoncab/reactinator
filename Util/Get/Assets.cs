namespace Reactinator.Util.Get;

public static class Assets
{
    // null when someone lost the asset
    public static byte[]? Read(string name)
    {
        using var stream = typeof(Assets).Assembly.GetManifestResourceStream(name);
        if (stream == null) return null;
        using var memory = new MemoryStream();
        stream.CopyTo(memory);
        return memory.ToArray();
    }
}
