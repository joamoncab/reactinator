namespace Reactinator;

// we keep everything in environment variables for security reasons
public record Config(string Token, string DbUri, string? AuthorizedUser, string? SoundsSource, string? CopypastaSource, string? SlashVersion)
{
    public static Config FromEnvironment()
    {
        LoadDotEnv();

        static string? Env(string name) =>
            Environment.GetEnvironmentVariable(name) is { Length: > 0 } value ? value.Trim() : null;

        var sounds = Env("SOUNDBOARD_URL");
        return new Config(
            Env("TOKEN") ?? throw new InvalidOperationException("no TOKEN, no reactinator"),
            Env("DB_URI") ?? throw new InvalidOperationException("no DB_URI, no memory"),
            Env("AUTHORIZED_USER"),
            sounds,
            Env("COPYPASTA_URL") ?? sounds,
            Env("SLASH_COMMANDS_VERSION"));
    }

    // .env is convenient for local runs; real environment variables always win
    private static void LoadDotEnv()
    {
        foreach (var path in new[] { Path.Combine(Directory.GetCurrentDirectory(), ".env"), Path.Combine(AppContext.BaseDirectory, ".env") }.Distinct())
        {
            if (!File.Exists(path)) continue;

            foreach (var raw in File.ReadAllLines(path))
            {
                var line = raw.Trim();
                if (line.Length == 0 || line.StartsWith('#')) continue;
                if (line.StartsWith("export ", StringComparison.Ordinal)) line = line[7..].TrimStart();

                var separator = line.IndexOf('=');
                if (separator <= 0) continue;

                var key = line[..separator].Trim();
                var value = line[(separator + 1)..].Trim();
                if (value.Length >= 2 && ((value[0] == '"' && value[^1] == '"') || (value[0] == '\'' && value[^1] == '\'')))
                    value = value[1..^1];

                if (Environment.GetEnvironmentVariable(key) is null) Environment.SetEnvironmentVariable(key, value);
            }
        }
    }
}
