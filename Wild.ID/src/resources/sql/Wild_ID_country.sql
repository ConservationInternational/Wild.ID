
CREATE TABLE  IF NOT EXISTS `country` (
  `country_id` int(3) NOT NULL AUTO_INCREMENT,
  `continent_id` int(2) NOT NULL,
  `name` varchar(128) NOT NULL,
  `code` varchar(4) NOT NULL,
  PRIMARY KEY (`country_id`),
  KEY `idx_country_name` (`name`),
  KEY `idx_country_code` (`code`),
  KEY `fk_country_continent` (`continent_id`),
  CONSTRAINT `fk_country_continent` FOREIGN KEY (`continent_id`) REFERENCES `continent` (`continent_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
