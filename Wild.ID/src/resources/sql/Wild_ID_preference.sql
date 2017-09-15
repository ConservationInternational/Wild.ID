CREATE TABLE IF NOT EXISTS `Wild_ID`.`preference` (
  `preference_id` int(3) NOT NULL AUTO_INCREMENT,
  `language` varchar(32) NOT NULL DEFAULT 'en',
  `style` varchar(32) NOT NULL DEFAULT 'Modena',
  `annotation_species_naming` varchar(32) NOT NULL,
  `annotation_default_person_id` int(11),
  `annotation_time_group_interval` int(11) NOT NULL DEFAULT 2,
  PRIMARY KEY (`preference_id`),
  KEY `fk_preference_annotation_person` (`annotation_default_person_id`),
  CONSTRAINT `fk_preference_annotation_person` FOREIGN KEY (`annotation_default_person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

