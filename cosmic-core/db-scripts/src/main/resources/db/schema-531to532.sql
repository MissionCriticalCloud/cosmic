--
-- Schema upgrade from 5.3.1 to 5.3.2;
--

-- New config properties for HAproxy loadbalancer
ALTER table load_balancing_rules ADD client_timeout int(10) NULL COMMENT 'client_timeout of haproxy config';
ALTER table load_balancing_rules ADD server_timeout int(10) NULL COMMENT 'server_timeout of haproxy config';
INSERT IGNORE INTO `cloud`.`configuration`(category, instance, component, name, value, description, default_value)
VALUES ('Network', 'DEFAULT', 'management-server', 'network.loadbalancer.haproxy.default.timeout.client', '60000', 'Default HAProxy client timeout setting (in ms)', '60000'),
('Network', 'DEFAULT', 'management-server', 'network.loadbalancer.haproxy.default.timeout.server', '60000', 'Default HAProxy server timeout setting (in ms)', '60000');

-- Add reference to ACLs for Public IPs
ALTER TABLE `cloud`.`user_ip_address` ADD COLUMN `ip_acl_id` bigint(20) unsigned NOT NULL AFTER `physical_network_id`;
UPDATE `cloud`.`user_ip_address` SET `cloud`.`user_ip_address`.`ip_acl_id` = 2;
