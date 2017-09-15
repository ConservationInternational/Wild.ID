
CREATE TABLE  IF NOT EXISTS `camera_trap` (
  `camera_trap_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `camera_trap_array_id` int(11) NOT NULL,
  PRIMARY KEY (`camera_trap_id`),
  KEY `fk_camera_trap_camera_trap_array` (`camera_trap_array_id`),
  CONSTRAINT `fk_camera_trap_camera_trap_array` FOREIGN KEY (`camera_trap_array_id`) REFERENCES `camera_trap_array` (`camera_trap_array_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
