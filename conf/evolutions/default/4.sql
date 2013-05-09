# --- Change column name to date_access

# --- !Ups

ALTER TABLE user CHANGE COLUMN date_login date_access timestamp;

# --- !Downs

ALTER TABLE user CHANGE COLUMN date_access date_login timestamp;
