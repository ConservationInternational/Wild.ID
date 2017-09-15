CREATE TABLE  IF NOT EXISTS `family_genus_species` (
  `family_genus_species_id` int(11) NOT NULL AUTO_INCREMENT,
  `class` varchar(64) DEFAULT NULL,
  `order_taxa` varchar(64) DEFAULT NULL,
  `family` varchar(64) DEFAULT NULL,
  `genus` varchar(64) DEFAULT NULL,
  `species` varchar(64) DEFAULT NULL,
  `iucn_species_id` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`family_genus_species_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
