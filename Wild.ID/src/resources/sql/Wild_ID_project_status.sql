
CREATE TABLE  IF NOT EXISTS  `project_status` (
  `project_status_id` int(11) NOT NULL AUTO_INCREMENT,
  `status` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`project_status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
