--;
-- Schema cleanup from 3.0.2 to 4.0.0;
--;

ALTER TABLE `cloud`.`domain_router` DROP COLUMN network_id;
