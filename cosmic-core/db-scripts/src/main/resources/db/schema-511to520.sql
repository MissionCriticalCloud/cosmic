--;
-- Schema upgrade from 5.1.1 to 5.2.0;
--;

-- Remove hypervisors we no longer support
UPDATE guest_os_hypervisor SET removed=now() WHERE hypervisor_type NOT IN ("KVM", "XenServer");

-- Remove old templates
UPDATE vm_template SET removed=now(), state='Inactive' WHERE `name` LIKE 'CentOS 5%' AND removed is NULL;

-- Remove any non-used guest_os_id
UPDATE `cloud`.`guest_os_hypervisor` SET removed=now() WHERE guest_os_id NOT IN (SELECT DISTINCT guest_os_id FROM vm_template WHERE state = 'active');
UPDATE `cloud`.`guest_os` SET removed=now() WHERE id NOT IN (SELECT DISTINCT guest_os_id FROM vm_template WHERE state = 'active');

-- keep only the defaults, as the others are very old
UPDATE `cloud`.`guest_os_hypervisor` SET removed=now() WHERE hypervisor_version <> 'default';

UPDATE guest_os SET display_name = 'Non-bootable ISO' WHERE id = 1;
UPDATE guest_os SET display_name = 'SystemVM Debian 7 (32 bit)' WHERE id = 183;
UPDATE guest_os SET display_name = 'SystemVM Debian 7 (64 bit)' WHERE id = 184;

-- VirtioIO capable
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1000, UUID(), 7, 'Default - VirtIO capable OS (64-bit)', utc_timestamp());

-- Linux
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1010, UUID(), 1, 'CentOS Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'CentOS Family', 1010, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1011, UUID(), 4, 'RHEL Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'RHEL Family', 1011, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1012, UUID(), 2, 'Debian Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Debian Family', 1012, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1013, UUID(), 10, 'Ubuntu Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Ubuntu Family', 1013, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1014, UUID(), 7, 'CoreOS Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'CoreOS Family', 1014, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1015, UUID(), 7, 'Linux Other', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Linux Other', 1015, utc_timestamp(), 0);

-- Windows
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1016, UUID(), 6, 'Windows 7 Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows 7 Family', 1016, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1017, UUID(), 6, 'Windows 8 Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows 8 Family', 1017, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1018, UUID(), 6, 'Windows 10 Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows 10 Family', 1018, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1019, UUID(), 6, 'Windows 2008 Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows 2008 Family', 1019, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1020, UUID(), 6, 'Windows 2012 Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows 2012 Family', 1020, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1021, UUID(), 6, 'Windows 2016 Family', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows 2016 Family', 1021, utc_timestamp(), 0);
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1022, UUID(), 6, 'Windows Other', now());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Windows Other', 1022, utc_timestamp(), 0);

-- Non-VirtIO Capable
INSERT IGNORE INTO `cloud`.`guest_os` (id, uuid, category_id, display_name, created) VALUES (1001, UUID(), 7, 'Non-VirtIO capable OS (64-bit)', utc_timestamp());
INSERT IGNORE INTO `cloud`.`guest_os_hypervisor` (uuid,hypervisor_type, hypervisor_version, guest_os_name, guest_os_id, created, is_user_defined) VALUES (UUID(),'KVM', 'default', 'Non-VirtIO capable OS (64-bit)', 1001, utc_timestamp(), 0);

-- Update KVM systemvm template to be VirtIO compatible on KVM
UPDATE vm_template SET guest_os_id = 1000 WHERE `name` LIKE 'SystemVM%' AND hypervisor_type = "KVM";
