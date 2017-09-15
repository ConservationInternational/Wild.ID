
CREATE TABLE IF NOT EXISTS `project_person_role` (
  `project_person_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `person_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_time` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`project_person_role_id`),
  KEY `fk_project_person_role_project` (`project_id`),
  KEY `fk_project_person_role_person` (`person_id`),
  KEY `fk_project_person_role_role` (`role_id`),
  CONSTRAINT `fk_project_person_role_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_project_person_role_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_project_person_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
