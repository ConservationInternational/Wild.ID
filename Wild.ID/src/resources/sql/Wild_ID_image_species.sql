
CREATE TABLE  IF NOT EXISTS `image_species` (
  `image_species_id` int(11) NOT NULL AUTO_INCREMENT,
  `image_id` int(11) NOT NULL,
  `family_genus_species_id` int(11) NOT NULL,
  `identify_person_id` int(11) NOT NULL,
  `individual_count` int(11) DEFAULT NULL,
  `uncertainty_type_id` int(11) NOT NULL,
  `homo_sapiens_type_id` int(4),
  `common_name_eng_id` INT(11) NULL,
  `subspecies` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`image_species_id`),
  KEY `fk_image_image_id` (`image_id`),
  KEY `fk_image_family_genus_species_id` (`family_genus_species_id`),
  KEY `fk_image_identify_person_id` (`identify_person_id`),
  CONSTRAINT `fk_image_family_genus_species_id` FOREIGN KEY (`family_genus_species_id`) REFERENCES `family_genus_species` (`family_genus_species_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_image_identify_person_id` FOREIGN KEY (`identify_person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_image_image_id` FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`) ON UPDATE CASCADE
  # CONSTRAINT `fk_image_common_name_eng_id` FOREIGN KEY (`common_name_eng_id`) REFERENCES `Wild_ID`.`common_name_eng` (`common_name_eng_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


