# behold, the react-inator!!1!
***
Bother people on Discord with reactions using this bot!
## How does it work
Given a set (by server mods) chance, the bot will react with a random custom emoji from the server.

Server mods can set a custom chance per channel as well

You can also talk to reactinator by pinging it! Super duper advanced ai! *not really ai and not super duper advanced

And finally, you can reply to someone mentioning @reactinator and querying a sound effect!
## Features
- React to messages with custom emojis
- Send sound effects
- Make custom mpreg emojis (don't question my sanity)
- Easy to set up and use
- Lightweight and efficient
- Configurable via slash commands
- Open source and free to use

## How to host this yourself?
1. create a bot on discord developers
2. on your host, download the jar file from releases
3. get the .sql file from source as well
4. run the sql queries on your mariadb server
5. set up the .env, which should be
```
AUTHORIZED_USER=your-discord-id
DB_URI=jdbc:mysql://mariadb-user:mariadb-password@mariadb-host:mariadb-port/database-name
FETCH_URL=url-for-sound-effects
SLASH_COMMANDS_VERSION=3 
TOKEN=discord-token
```
> NOTE: SLASH_COMMANDS_VERSION is bumped with every slash commands update
6. run the jar
7. enjoy your reactinator!