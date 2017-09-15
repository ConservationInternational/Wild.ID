CREATE TABLE IF NOT EXISTS `camera_model_exif_feature` (
  `camera_model_exif_feature_id` int(11) NOT NULL AUTO_INCREMENT,
  `camera_model_id` int(11) NOT NULL,
  `exif_tag_name` varchar(64) NOT NULL,
  `exif_tag_value` varchar(256) NOT NULL,
  `secondary_tag_delimit` varchar(1) DEFAULT NULL,
  `secondary_tag_name` varchar(16) DEFAULT NULL,
  `image_feature_id` int(11) DEFAULT NULL,  
  PRIMARY KEY (`camera_model_exif_feature_id`),
  KEY `fk_camera_model_exif_feature_camera_model` (`camera_model_id`),
  KEY `fk_camera_model_exif_feature_image_feature` (`image_feature_id`),
  CONSTRAINT `fk_camera_model_exif_feature_camera_model` FOREIGN KEY (`camera_model_id`) REFERENCES `camera_model` (`camera_model_id`) ,
  CONSTRAINT `fk_camera_model_exif_feature_image_feature` FOREIGN KEY (`image_feature_id`) REFERENCES `image_feature` (`image_feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
