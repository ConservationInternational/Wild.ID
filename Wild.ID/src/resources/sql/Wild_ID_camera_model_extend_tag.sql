
CREATE TABLE  IF NOT EXISTS `camera_model_extend_tag` (
  `camera_model_extend_tag_id` int(11) NOT NULL AUTO_INCREMENT,
  `camera_model_id` int(11) NOT NULL,
  `name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`camera_model_extend_tag_id`),
  KEY `fk_camera_model_extend_tag_camera_model_id` (`camera_model_id`),
  CONSTRAINT `fk_camera_model_extend_tag_camera_model_id` FOREIGN KEY (`camera_model_id`) REFERENCES `camera_model` (`camera_model_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

