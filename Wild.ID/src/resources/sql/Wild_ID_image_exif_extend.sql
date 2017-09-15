
CREATE TABLE  IF NOT EXISTS `image_exif_extend` (
  `image_exif_extend_id` int(11) NOT NULL AUTO_INCREMENT,
  `image_id` int(11) NOT NULL,
  `camera_model_extend_tag_id` int(11) DEFAULT NULL,
  `value` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`image_exif_extend_id`),
  KEY `fk_image_exif_extend_image_id` (`image_id`),
  KEY `fk_image_exif_extend_camera_model_extend_tag_id` (`camera_model_extend_tag_id`),
  CONSTRAINT `fk_image_exif_extend_camera_model_extend_tag_id` FOREIGN KEY (`camera_model_extend_tag_id`) REFERENCES `camera_model_extend_tag` (`camera_model_extend_tag_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_image_exif_extend_image_id` FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
