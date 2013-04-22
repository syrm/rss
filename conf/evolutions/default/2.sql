# --- Init database schema

# --- !Ups

ALTER TABLE user ADD date_login timestamp DEFAULT current_timestamp;
UPDATE user SET date_login=date;

# --- !Downs

ALTER TABLE user DROP date_login;