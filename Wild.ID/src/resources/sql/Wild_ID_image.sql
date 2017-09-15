
CREATE TABLE  IF NOT EXISTS `image` (
  `image_id` int(11) NOT NULL AUTO_INCREMENT,
  `image_sequence_id` int(11) NOT NULL,
  `time_captured` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `raw_name` varchar(128) NOT NULL,
  `system_name` varchar(128) DEFAULT NULL,
  `note` text,
  `restrictions_on_access` text,
  `display_flag` varchar(128) DEFAULT NULL,
  `display_flag_reason` text,
  `embargo_period` varchar(128) DEFAULT NULL,
  `image_type_id` int(11) DEFAULT NULL,
  `image_type_identify_person_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`image_id`),
  KEY `fk_image_image_type` (`image_type_id`),
  KEY `fk_image_image_type_identify_person_id` (`image_type_identify_person_id`),
  KEY `fk_image_image_sequence` (`image_sequence_id`),
  CONSTRAINT `fk_image_image_sequence` FOREIGN KEY (`image_sequence_id`) REFERENCES `image_sequence` (`image_sequence_id`),
  CONSTRAINT `fk_image_image_type` FOREIGN KEY (`image_type_id`) REFERENCES `image_type` (`image_type_id`) ON UPDATE NO ACTION,
  CONSTRAINT `fk_image_image_type_identify_person_id` FOREIGN KEY (`image_type_identify_person_id`) REFERENCES `person` (`person_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
