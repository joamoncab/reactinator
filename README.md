# behold, the react-inator!!1!
***
Bother people on Discord with reactions using this bot!

Now rewritten in C#, because the JVM was eating more RAM than the reactions deserved.

## How does it work
Given a set (by server mods) chance, the bot reacts with a random custom emoji from the server.
The more an emoji gets used by actual humans in a channel (or by the message author), the higher its chance of being picked. Every emoji still keeps a base chance, because chaos must survive.

Server mods can set a custom chance per channel as well.

You can also talk to reactinator by pinging it! Super duper advanced ai! *not really ai and not super duper advanced. Everything it says lives in `Assets/replies.json`, so go wild.

And finally, you can reply to someone mentioning @reactinator and querying a sound effect.

## Features
- React to messages with custom emojis
- Learn which emojis people actually use and weight the random pick
- Send sound effects
- Make custom mpreg emojis (don't question my sanity)
- Live-updating reactboard, leaderboards and per-user reaction stats
- Opt-outs and blacklists for users and emojis
- Configurable via slash commands
- Lightweight and efficient (for real this time, it's a native binary)
- Open source and free to use

## How to host this yourself?
1. create a bot on the [Discord developer portal](https://discord.com/developers/applications). no privileged intents are needed: message content is only read from messages that mention the bot, which Discord allows without approval
2. on your host, download the binary from releases, or build it yourself with `dotnet publish -c Release -r linux-x64`
3. get the `.sql` files from source as well
4. run the sql queries on your MariaDB server:
   - fresh install: run `INITIAL_QUERY.sql`
   - upgrading an existing database: run `UPDATE_USER_REACTIONS.sql`, `LEARN_REACTIONS.sql`, `REACTBOARD_EDITS.sql` and `REMOVE_QUOTES.sql`
   - all the scripts are idempotent, so running them twice won't hurt
5. set up the `.env` (it's loaded automatically from the working directory, or next to the binary):
```
AUTHORIZED_USER=your-discord-id
DB_URI=jdbc:mysql://mariadb-user:mariadb-password@mariadb-host:mariadb-port/database-name
SOUNDBOARD_URL=url-for-sound-effects
COPYPASTA_URL=url-for-copypastas
SLASH_COMMANDS_VERSION=8
TOKEN=discord-token
```
> NOTE: `SLASH_COMMANDS_VERSION` is bumped with every slash commands update. Bump it on existing installs so Discord drops commands that no longer exist. Guild commands refresh on the next message; user-installable commands refresh on startup.

> NOTE: both urls are meant to receive the base endpoint + `{query}` and reply with the media (mp3 file or plain text) directly.

> NOTE: `DB_URI` also takes a plain MySqlConnector connection string, if you're fancy. Plain environment variables override anything in `.env`.

6. run the binary (or `dotnet run`)
7. enjoy your reactinator!

## Commands
- `/chances [channel]` — see the current guild or channel reaction chance
- `/setchance <chance>` — set the guild reaction chance (mods only)
- `/setchancechannel <chance> [channel]` — set a per-channel chance (mods only)
- `/mpreg <user>` — put someone's pfp on discord's mpreg emoji
- `/soundboard <sound>` — play a soundboard effect
- `/copypasta <copypasta>` — send a copypasta
- `/optout <mpreg|react>` — toggle opt-outs
- `/blacklistemoji <emoji>` — toggle an emoji blacklist (mods only)
- `/blacklist <user>` — toggle a user blacklist (bot owner only)
- `/reactboard <channel> <threshold>` — configure the reactboard (mods only, threshold 0 disables it)
- `/emojileaderboard`, `/reactleaderboard`, `/userleaderboard`, `/userstats [user]` — stats
- `/refreshcommands` — refresh slash commands (bot owner only)

## Building
You need the .NET 10 SDK. Native AOT also needs clang on Linux or MSVC on Windows (see the [native AOT prerequisites](https://aka.ms/nativeaot-prerequisites)).
```
dotnet run                                      # just run it
dotnet publish -c Release -r linux-x64          # native binary for linux x64
dotnet publish -c Release -r <rid>              # also linux-arm64, win-x64 or win-arm64
```

GitHub Actions builds all supported RIDs on their native runners and uploads one artifact per platform.

The bot and its assets are embedded into the binary. If you edit `Assets/replies.json`, rebuild it.
