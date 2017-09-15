
CREATE TABLE IF NOT EXISTS `project` (
  `project_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `short_name` varchar(16) DEFAULT NULL,
  `abbrev_name` varchar(8) NOT NULL,
  `objective` text,
  `use_and_constraints` text,
  `start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_time` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `time_zone` varchar(50) DEFAULT NULL,
  `country_id` int(3) DEFAULT NULL,
  `contact_person_id` int(11) DEFAULT NULL,
  `last_update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `respository_id` int(11) DEFAULT NULL,
  `respository_last_sync_time` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  `project_status_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  KEY `fk_project_country` (`country_id`),
  KEY `fk_project_contact_person` (`contact_person_id`),
  KEY `fk_project_status_status_idx` (`project_status_id`),
  CONSTRAINT `fk_project_contact_person` FOREIGN KEY (`contact_person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_project_country` FOREIGN KEY (`country_id`) REFERENCES `country` (`country_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_project_status_status` FOREIGN KEY (`project_status_id`) REFERENCES `project_status` (`project_status_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

