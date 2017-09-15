
CREATE TABLE  IF NOT EXISTS `image_homo_sapiens_type` (
  `image_homo_sapiens_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `image_id` int(11) NOT NULL,
  `homo_sapiens_type_id` int(4) NOT NULL,
  PRIMARY KEY (`image_homo_sapiens_type_id`),
  KEY `fk_image_homo_sapiens_type_image_id` (`image_id`),
  KEY `fk_image_homo_sapiens_type_homo_sapiens_type_id` (`homo_sapiens_type_id`),
  CONSTRAINT `fk_image_homo_sapiens_type_homo_sapiens_type_id` FOREIGN KEY (`homo_sapiens_type_id`) REFERENCES `homo_sapiens_type` (`homo_sapiens_type_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_image_homo_sapiens_type_image_id` FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
