-- Delete unused columns
ALTER TABLE `port_forwarding_rules`
  DROP COLUMN `dest_port_end`;
ALTER TABLE `firewall_rules`
  DROP COLUMN `end_port`;

-- Rename columns
ALTER TABLE `port_forwarding_rules`
  CHANGE COLUMN `dest_port_start` `dest_port` INT(10) NOT NULL;
ALTER TABLE `firewall_rules`
  CHANGE COLUMN `start_port` `port` INT(10) DEFAULT NULL
