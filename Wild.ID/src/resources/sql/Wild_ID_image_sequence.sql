
CREATE TABLE  IF NOT EXISTS `image_sequence` (
  `image_sequence_id` int(11) NOT NULL AUTO_INCREMENT,
  `deployment_id` int(11) NOT NULL,
  PRIMARY KEY (`image_sequence_id`),
  KEY `fk_image_sequence_deployment` (`deployment_id`),
  CONSTRAINT `fk_image_sequence_deployment` FOREIGN KEY (`deployment_id`) REFERENCES `deployment` (`deployment_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
