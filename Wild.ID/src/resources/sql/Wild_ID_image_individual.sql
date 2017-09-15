
CREATE TABLE  IF NOT EXISTS `image_individual` (
  `image_individual_id` int(11) NOT NULL AUTO_INCREMENT,
  `image_species_id` int(11) NOT NULL,
  `name` varchar(32) DEFAULT NULL,
  `sex` varchar(8) DEFAULT NULL,
  `age_id` int DEFAULT NULL,
  `x` int NOT NULL,
  `y` int NOT NULL,
  `note` text,
  PRIMARY KEY (`image_individual_id`),
  KEY `fk_image_individual_image_species_id` (`image_species_id`),
  CONSTRAINT `fk_image_individual_image_species_id` FOREIGN KEY (`image_species_id`) REFERENCES `image_species` (`image_species_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_image_individual_age_id` FOREIGN KEY (`age_id`) REFERENCES `age` (`age_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
