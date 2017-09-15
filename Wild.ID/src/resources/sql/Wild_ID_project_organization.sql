

CREATE TABLE IF NOT EXISTS `project_organization` (
  `project_id` int(11) NOT NULL,
  `organization_id` int(11) NOT NULL,
  KEY `fk_project_organization_project` (`project_id`),
  KEY `fk_project_organization_organization` (`organization_id`),
  CONSTRAINT `fk_project_organization_organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`organization_id`),
  CONSTRAINT `fk_project_organization_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
