--;
-- Schema cleanup from 4.1.0 to 4.2.0;
--;

#have to drop the foreign key in order to delete primary key; will re-insert the foreign key later
ALTER TABLE `cloud`.`remote_access_vpn` DROP foreign key `fk_remote_access_vpn__vpn_server_addr_id`;
ALTER TABLE `cloud`.`remote_access_vpn` DROP primary key;
ALTER TABLE `cloud`.`remote_access_vpn` ADD primary key (`id`);
ALTER TABLE `cloud`.`remote_access_vpn` ADD CONSTRAINT `fk_remote_access_vpn__vpn_server_addr_id` FOREIGN KEY (`vpn_server_addr_id`) REFERENCES `user_ip_address` (`id`);


