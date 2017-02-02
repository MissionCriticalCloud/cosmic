--;
-- Schema upgrade from 5.3.0 to 5.3.1;
--;

-- Increase URL fields to 2048 charachters to match the VO
ALTER TABLE `cloud`.`volume_host_ref` MODIFY COLUMN `url` varchar(2048);
ALTER TABLE `cloud`.`object_datastore_ref` MODIFY COLUMN `url` varchar(2048);
ALTER TABLE `cloud`.`image_store` MODIFY COLUMN `url` varchar(2048);
ALTER TABLE `cloud`.`template_store_ref` MODIFY COLUMN `url` varchar(2048);
ALTER TABLE `cloud`.`volume_store_ref` MODIFY COLUMN `url` varchar(2048);
ALTER TABLE `cloud`.`volume_store_ref` MODIFY COLUMN `download_url` varchar(2048);
