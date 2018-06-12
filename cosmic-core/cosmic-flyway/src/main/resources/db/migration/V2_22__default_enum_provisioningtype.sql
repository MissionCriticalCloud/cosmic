-- Change all to UPPERCASE
ALTER TABLE `disk_offering` ALTER `provisioning_type` SET DEFAULT 'THIN';
ALTER TABLE `volumes` ALTER `provisioning_type` SET DEFAULT 'THIN';

UPDATE `disk_offering` SET `provisioning_type` = UPPER(`provisioning_type`);
UPDATE `volumes` SET `provisioning_type` = UPPER(`provisioning_type`);
