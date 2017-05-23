--
-- Schema upgrade from 5.3.5 to 5.3.6;
--

-- Add source nat list
ALTER TABLE `cloud`.`vpc` ADD COLUMN `source_nat_list` varchar(255) DEFAULT NULL COMMENT 'List of CIDRS to source NAT on VPC' AFTER `redundant`;
