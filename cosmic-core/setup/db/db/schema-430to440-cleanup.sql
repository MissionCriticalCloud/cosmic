--;
-- Schema cleanup from 4.3.0 to 4.4.0;
--;

ALTER TABLE `cloud`.`network_acl_item` DROP COLUMN `cidr`;
