#Schema cleanup from 3.0.1 to 3.0.2;


DROP TABLE IF EXISTS `cloud`.`network_tags`;

ALTER TABLE `cloud`.`nics` MODIFY `vm_type` varchar(32) DEFAULT NULL;
ALTER TABLE `cloud`.`service_offering` MODIFY `default_use` tinyint(1) UNSIGNED NOT NULL DEFAULT '0';
ALTER TABLE `cloud`.`snapshots` MODIFY `hypervisor_type` varchar(32) NOT NULL;
ALTER TABLE `cloud`.`snapshots` MODIFY `version` varchar(32) DEFAULT NULL;
ALTER TABLE `cloud`.`volumes` MODIFY `state` varchar(32) DEFAULT NULL;


ALTER TABLE `cloud_usage`.`usage_ip_address` MODIFY `id` bigint(20) UNSIGNED NOT NULL;
ALTER TABLE `cloud_usage`.`usage_ip_address` MODIFY  `is_source_nat` smallint(1) NOT NULL;
ALTER TABLE `cloud_usage`.`usage_network` MODIFY `host_id` bigint(20) UNSIGNED NOT NULL;
ALTER TABLE `cloud_usage`.`usage_network` MODIFY `host_type` varchar(32) DEFAULT NULL;
ALTER TABLE `cloud_usage`.`user_statistics` MODIFY `device_id` bigint(20) UNSIGNED NOT NULL;
