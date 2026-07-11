-- User reaction tracking tables
CREATE TABLE USER_REACTIONS
(
    user_id  BIGINT NOT NULL,
    guild_id BIGINT NOT NULL,
    total    INT DEFAULT 0,
    PRIMARY KEY (user_id, guild_id),
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    FOREIGN KEY (guild_id) REFERENCES GUILDS (id) ON DELETE CASCADE
);

CREATE TABLE USER_EMOJI_STATS
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
