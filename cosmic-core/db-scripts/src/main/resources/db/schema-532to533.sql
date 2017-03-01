--
-- Schema upgrade from 5.3.2 to 5.3.3;
--

-- VirtIO-SCSI
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1002, UUID(), 7, 'VirtIO-SCSI capable OS (64-bit)', utc_timestamp());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'VirtIO-SCSI capable OS', 1002, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1023, UUID(), 6, 'Windows VirtIO-SCSI', utc_timestamp());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows VirtIO-SCSI', 1023, utc_timestamp(), 0);

-- URL field length
ALTER TABLE `cloud`.`template_store_ref` MODIFY COLUMN `download_url` varchar(2048);

-- Redundant routers separate offering for second router
ALTER table vpc_offerings ADD `secondary_service_offering_id` bigint(20) unsigned DEFAULT NULL COMMENT 'service offering id that a secondary virtual router is tied to';
ALTER table network_offerings ADD `secondary_service_offering_id` bigint(20) unsigned DEFAULT NULL COMMENT 'service offering id that a secondary virtual router is tied to';

