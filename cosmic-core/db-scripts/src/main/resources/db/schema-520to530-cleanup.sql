--;
-- Schema cleanup from 5.2.0 to 5.3.0;
--;

-- Backup table for later reference
CREATE TABLE vpc_gateways_pre530 LIKE vpc_gateways;
INSERT vpc_gateways_pre530 SELECT * FROM vpc_gateways;

-- delete from original
ALTER TABLE `vpc_gateways` DROP COLUMN `gateway`;
ALTER TABLE `vpc_gateways` DROP COLUMN `netmask`;
ALTER TABLE `vpc_gateways` DROP COLUMN `vlan_tag`;
