# --- Init database schema

# --- !Ups

ALTER TABLE item CHANGE COLUMN url url varchar(400);
UPDATE item SET guid=md5(guid);
ALTER TABLE item CHANGE COLUMN guid guid varchar(32) NOT NULL;

# --- !Downs

ALTER TABLE item CHANGE COLUMN url url varchar(255) NOT NULL;
ALTER TABLE item CHANGE COLUMN guid guid varchar(255) NOT NULL;