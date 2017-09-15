
CREATE TABLE  IF NOT EXISTS `dual_deployment` (
  `dual_deployment_id` int(11) NOT NULL AUTO_INCREMENT,
  `deployment_1_id` int(11) NOT NULL,
  `deployment_2_id` int(11) NOT NULL,
  PRIMARY KEY (`dual_deployment_id`),
  KEY `fk_dual_deployment_1` (`deployment_1_id`),
  KEY `fk_dual_deployment_2` (`deployment_2_id`),
  CONSTRAINT `fk_dual_deployment_1` FOREIGN KEY (`deployment_1_id`) REFERENCES `deployment` (`deployment_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_dual_deployment_2` FOREIGN KEY (`deployment_2_id`) REFERENCES `deployment` (`deployment_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
