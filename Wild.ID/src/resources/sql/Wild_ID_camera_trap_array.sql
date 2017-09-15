
CREATE TABLE  IF NOT EXISTS  `camera_trap_array` (
  `camera_trap_array_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `project_id` int(11) NOT NULL,
  PRIMARY KEY (`camera_trap_array_id`),
  KEY `fk_camera_trap_array_project` (`project_id`),
  CONSTRAINT `fk_camera_trap_array_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
