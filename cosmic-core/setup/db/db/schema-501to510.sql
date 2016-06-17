--;
-- Schema upgrade from 5.0.1 to 5.1.0;
--;

-- Get rid of old foreign keys
ALTER TABLE `cloud`.`static_routes` DROP FOREIGN KEY `fk_static_routes__vpc_gateway_id`;
ALTER TABLE `cloud`.`static_routes` DROP FOREIGN KEY `fk_static_routes__vpc_id`;
ALTER TABLE `cloud`.`static_routes` DROP FOREIGN KEY `fk_static_routes__account_id`;
ALTER TABLE `cloud`.`static_routes` DROP FOREIGN KEY `fk_static_routes__domain_id`;

-- Backup table for later reference
RENAME TABLE `cloud`.`static_routes` TO `cloud`.`static_routes_pre510`;

-- Create new table
CREATE TABLE `cloud`.`static_routes` (
  `id` bigint unsigned NOT NULL auto_increment COMMENT 'id',
  `uuid` varchar(40),
  `cidr` varchar(18) COMMENT 'cidr for the static route',
  `gateway_ip_address` varchar(45) COMMENT 'gateway ip address for the static route',
  `metric` INT(10) DEFAULT 100 COMMENT 'metric value for the route',
  `state` char(32) NOT NULL COMMENT 'current state of this rule',
  `vpc_id` bigint unsigned COMMENT 'vpc the firewall rule is associated with',
  `account_id` bigint unsigned NOT NULL COMMENT 'owner id',
  `domain_id` bigint unsigned NOT NULL COMMENT 'domain id',
  `created` datetime COMMENT 'Date created',
  PRIMARY KEY  (`id`),
  CONSTRAINT `fk_static_routes__vpc_id` FOREIGN KEY (`vpc_id`) REFERENCES `vpc`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_static_routes__account_id` FOREIGN KEY(`account_id`) REFERENCES `account`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_static_routes__domain_id` FOREIGN KEY(`domain_id`) REFERENCES `domain`(`id`) ON DELETE CASCADE,
  CONSTRAINT `uc_static_routes__uuid` UNIQUE (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Migrate data from old to new
INSERT INTO `cloud`.`static_routes`
(`id`, `uuid`, `cidr`, `gateway_ip_address`, `state`, `vpc_id`, `account_id`, `domain_id`, `created`)
SELECT  `static_routes_pre510`.id,
  `static_routes_pre510`.uuid,
  `static_routes_pre510`.cidr,
  `vpc_gateways`.ip4_address,
  `static_routes_pre510`.state,
  `static_routes_pre510`.vpc_id,
  `static_routes_pre510`.account_id,
  `static_routes_pre510`.domain_id,
  `static_routes_pre510`.created
FROM `cloud`.`static_routes_pre510`, `cloud`.vpc_gateways
WHERE `static_routes_pre510`.vpc_gateway_id = vpc_gateways.id;
