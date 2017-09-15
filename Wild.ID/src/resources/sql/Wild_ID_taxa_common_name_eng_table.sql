CREATE TABLE  IF NOT EXISTS Wild_ID.`common_name_eng` (
  `common_name_eng_id` int(11) NOT NULL AUTO_INCREMENT,
  `family_genus_species_id` int(11) NOT NULL,
  `name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`common_name_eng_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
