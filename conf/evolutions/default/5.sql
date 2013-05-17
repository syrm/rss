# --- Add column last_error, last_update for feed

# --- !Ups

ALTER TABLE feed ADD last_update timestamp null;
ALTER TABLE feed ADD last_error varchar(255) null;

# --- !Downs

ALTER TABLE feed DROP last_update;
ALTER TABLE feed DROP last_error;