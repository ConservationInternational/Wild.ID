
CREATE TABLE  IF NOT EXISTS `camera_model` (
  `camera_model_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `maker` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`camera_model_id`),
  KEY `idx_camera_model_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

