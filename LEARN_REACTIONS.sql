-- reactinator learns what people react with, so the random pick gets less random
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

-- the meanie message only goes out once per user
ALTER TABLE USERS ADD COLUMN IF NOT EXISTS block_warned BOOLEAN DEFAULT FALSE;
