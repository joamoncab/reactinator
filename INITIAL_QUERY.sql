-- full schema for a fresh reactinator install
-- already have a database? run the upgrade scripts instead (see README)

-- 1. create independent tables
CREATE TABLE IF NOT EXISTS GUILDS
(
    id        BIGINT PRIMARY KEY,
    chances   INT,
    slash_ver VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS USERS
(
    id           BIGINT PRIMARY KEY,
    blacklisted  BOOLEAN DEFAULT FALSE,
    block_warned BOOLEAN DEFAULT FALSE
);

-- 2. create tables depending on GUILDS
CREATE TABLE IF NOT EXISTS CHANNEL
(
    id      BIGINT PRIMARY KEY,
    chances INT,
    guild   BIGINT NOT NULL,
    FOREIGN KEY (guild) REFERENCES GUILDS (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS EMOJI
(
    id          BIGINT PRIMARY KEY,
    times_used  INT     DEFAULT 0,
    blacklisted BOOLEAN DEFAULT FALSE,
    guild       BIGINT NOT NULL,
    FOREIGN KEY (guild) REFERENCES GUILDS (id) ON DELETE CASCADE
);

-- 3. create tables depending on USERS
CREATE TABLE IF NOT EXISTS OPTOUTS
(
    id    BIGINT PRIMARY KEY,
    mpreg BOOLEAN DEFAULT FALSE,
    react BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id) REFERENCES USERS (id) ON DELETE CASCADE
);

-- 4. create tables depending on CHANNEL
CREATE TABLE IF NOT EXISTS REACTBOARD
(
    channel   BIGINT PRIMARY KEY,
    threshold INT NOT NULL,
    FOREIGN KEY (channel) REFERENCES CHANNEL (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS MESSAGES
(
    id                 BIGINT PRIMARY KEY,
    channel            BIGINT NOT NULL,
    reactboard_message BIGINT,
    FOREIGN KEY (channel) REFERENCES CHANNEL (id) ON DELETE CASCADE
);

-- 5. create tables depending on MESSAGES, EMOJI, and USERS
CREATE TABLE IF NOT EXISTS REACTION
(
    message BIGINT PRIMARY KEY,
    count   INT DEFAULT 1,
    emoji   BIGINT NOT NULL,
    FOREIGN KEY (message) REFERENCES MESSAGES (id) ON DELETE CASCADE,
    FOREIGN KEY (emoji) REFERENCES EMOJI (id) ON DELETE CASCADE
);

-- 6. user reaction stats
CREATE TABLE IF NOT EXISTS USER_REACTIONS
(
    user_id  BIGINT NOT NULL,
    guild_id BIGINT NOT NULL,
    total    INT DEFAULT 0,
    PRIMARY KEY (user_id, guild_id),
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    FOREIGN KEY (guild_id) REFERENCES GUILDS (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS USER_EMOJI_STATS
(
    user_id  BIGINT NOT NULL,
    guild_id BIGINT NOT NULL,
    emoji_id BIGINT NOT NULL,
    count    INT DEFAULT 1,
    PRIMARY KEY (user_id, guild_id, emoji_id),
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    FOREIGN KEY (guild_id) REFERENCES GUILDS (id) ON DELETE CASCADE,
    FOREIGN KEY (emoji_id) REFERENCES EMOJI (id) ON DELETE CASCADE
);

-- 7. what hoomans actually react with, used to weight the random pick
CREATE TABLE IF NOT EXISTS CHANNEL_EMOJI_STATS
(
    channel_id BIGINT NOT NULL,
    emoji_id   BIGINT NOT NULL,
    count      INT DEFAULT 1,
    PRIMARY KEY (channel_id, emoji_id),
    FOREIGN KEY (channel_id) REFERENCES CHANNEL (id) ON DELETE CASCADE,
    FOREIGN KEY (emoji_id) REFERENCES EMOJI (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS AUTHOR_EMOJI_STATS
(
    user_id  BIGINT NOT NULL,
    emoji_id BIGINT NOT NULL,
    count    INT DEFAULT 1,
    PRIMARY KEY (user_id, emoji_id),
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    FOREIGN KEY (emoji_id) REFERENCES EMOJI (id) ON DELETE CASCADE
);
