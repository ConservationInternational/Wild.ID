CREATE TABLE IF NOT EXISTS `image_feature` (
  `image_feature_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `default_exif_tag_name` varchar(64) NOT NULL,
  PRIMARY KEY (`image_feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
