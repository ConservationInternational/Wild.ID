
CREATE TABLE  IF NOT EXISTS `camera` (
  `camera_id` int(11) NOT NULL AUTO_INCREMENT,
  `camera_model_id` int(11) NOT NULL,
  `serial_number` varchar(128) DEFAULT NULL,
  `year_purchased` int(2) DEFAULT NULL,
  `project_id` int(11) NOT NULL,
  PRIMARY KEY (`camera_id`),
  KEY `fk_camera_camera_model` (`camera_model_id`),
  KEY `FK_camera_project` (`project_id`),
  CONSTRAINT `fk_camera_camera_model` FOREIGN KEY (`camera_model_id`) REFERENCES `camera_model` (`camera_model_id`) ON UPDATE CASCADE,
  CONSTRAINT `FK_camera_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


