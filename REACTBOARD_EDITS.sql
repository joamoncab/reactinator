-- lets the reactboard keep its post updated as a message gets more reactions (shame i didn't do this before lmao)
ALTER TABLE MESSAGES ADD COLUMN IF NOT EXISTS reactboard_message BIGINT;
