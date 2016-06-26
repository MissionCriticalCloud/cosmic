#Schema upgrade from 3.0.6 to 3.0.7;

INSERT IGNORE INTO `cloud`.`configuration` VALUES ('Network', 'DEFAULT', 'management-server', 'network.loadbalancer.haproxy.max.conn', '4096', 'Load Balancer(haproxy) maximum number of concurrent connections(global max)');

ALTER TABLE `cloud`.`network_offerings` ADD COLUMN `concurrent_connections` int(10) unsigned COMMENT 'concurrent connections supported on this network';
