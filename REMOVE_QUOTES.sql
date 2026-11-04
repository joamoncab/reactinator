-- removes the retired quote card feature from older databases
DROP TABLE IF EXISTS QUOTE_TAMPER;
ALTER TABLE OPTOUTS DROP COLUMN IF EXISTS quote;
