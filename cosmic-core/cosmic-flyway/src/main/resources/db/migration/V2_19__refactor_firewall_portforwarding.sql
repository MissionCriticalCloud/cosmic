-- Delete unused columns
ALTER TABLE `port_forwarding_rules`
  DROP COLUMN `dest_port_end`;
ALTER TABLE `firewall_rules`
  DROP COLUMN `end_port`;
ALTER TABLE `firewall_rules`
  DROP COLUMN `type`;

ALTER TABLE `firewall_rules`
  DROP FOREIGN KEY `fk_firewall_rules__related`;
ALTER TABLE `firewall_rules`
  DROP COLUMN `related`;

-- Rename columns
ALTER TABLE `port_forwarding_rules`
  CHANGE COLUMN `dest_port_start` `dest_port` INT(10) NOT NULL;
ALTER TABLE `firewall_rules`
  CHANGE COLUMN `start_port` `port` INT(10) DEFAULT NULL;

-- Drop tables
DROP TABLE IF EXISTS `firewall_rules_cidrs`;