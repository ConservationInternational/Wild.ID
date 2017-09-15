

CREATE TABLE  IF NOT EXISTS  `event` (
  `event_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `description` text,
  `project_id` int(11) NOT NULL,
  PRIMARY KEY (`event_id`),
  KEY `idx_event_name` (`name`),
  KEY `fk_event_project` (`project_id`),
  CONSTRAINT `fk_event_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
