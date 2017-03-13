--
-- Schema upgrade from 5.3.3 to 5.3.4;
--

-- Add ip exclusion list
ALTER TABLE `cloud`.`networks` ADD COLUMN `ip_exclusion_list` varchar(255) DEFAULT NULL COMMENT 'IP list excluded from assignment' AFTER `redundant`;

