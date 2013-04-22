# --- Init database schema

# --- !Ups

CREATE TABLE user (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  salt varchar(255) NOT NULL DEFAULT '',
  permission varchar(50) NOT NULL DEFAULT 'NormalUser',
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  token varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name (name),
  UNIQUE KEY email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE feed (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  url varchar(255) NOT NULL DEFAULT '',
  site varchar(255) NOT NULL DEFAULT '',
  name varchar(255) NOT NULL,
  favicon varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name (name),
  UNIQUE KEY url (url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE item (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  url varchar(255) NOT NULL,
  date timestamp NULL DEFAULT NULL,
  feed_id int(10) unsigned NOT NULL,
  content longtext NOT NULL,
  title varchar(255) NOT NULL,
  guid varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (id),
  UNIQUE KEY feed_id (feed_id,guid),
  CONSTRAINT fk_item_feed_id FOREIGN KEY (feed_id) REFERENCES feed (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE bookmark (
  user_id int(10) unsigned NOT NULL,
  item_id int(10) unsigned NOT NULL DEFAULT '0',
  date timestamp NULL DEFAULT NULL,
  PRIMARY KEY (user_id,item_id),
  KEY fk_bookmark_item_id (item_id),
  CONSTRAINT fk_bookmark_item_id FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `read` (
  item_id int(10) unsigned NOT NULL,
  user_id int(10) unsigned NOT NULL DEFAULT '0',
  date timestamp NULL DEFAULT NULL,
  PRIMARY KEY (item_id,user_id),
  KEY fk_read_user_id (user_id),
  CONSTRAINT fk_read_user_id FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_read_item_id FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE subscription (
  user_id int(10) unsigned NOT NULL DEFAULT '0',
  feed_id int(10) unsigned NOT NULL DEFAULT '0',
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id,feed_id),
  KEY fk_subscription_feed_id (feed_id),
  CONSTRAINT fk_subscription_feed_id FOREIGN KEY (feed_id) REFERENCES feed (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_subscription_user_id FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs

DROP TABLE IF EXISTS subscription;
DROP TABLE IF EXISTS `read`;
DROP TABLE IF EXISTS bookmark;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS feed;
DROP TABLE IF EXISTS user;
