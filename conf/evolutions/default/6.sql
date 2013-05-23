# --- Add column kind for feed

# --- !Ups

ALTER TABLE feed ADD kind enum('rss', 'atom') NOT NULL;
UPDATE feed set kind = 'rss';

# --- !Downs

ALTER TABLE feed DROP kind;
