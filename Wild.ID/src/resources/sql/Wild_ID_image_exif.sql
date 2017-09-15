
CREATE TABLE  IF NOT EXISTS `image_exif` (
  `image_exif_id` int(11) NOT NULL AUTO_INCREMENT,
  `image_id` int(11) NOT NULL,
  `image_feature_id` INT(11) NOT NULL,
  `exif_tag_value` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`image_exif_id`),
  KEY `fk_image_exif_image_id` (`image_id`),
  CONSTRAINT `fk_image_exif_image_id` FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


