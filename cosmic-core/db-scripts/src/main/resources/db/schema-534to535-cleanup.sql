--
-- Schema cleanup from 5.3.4 to 5.3.5;
--

ALTER TABLE `cloud`.`snapshots` DROP FOREIGN KEY `fk_snapshots__s3_id`;
ALTER TABLE `cloud`.`snapshots` DROP COLUMN `s3_id`;

DROP TABLE IF EXISTS `cloud`.`template_s3_ref`;
DROP TABLE IF EXISTS `cloud`.`s3`;

ALTER TABLE `cloud`.`snapshots` DROP COLUMN `swift_id`;
DROP TABLE IF EXISTS `cloud`.`template_swift_ref`;
DROP TABLE IF EXISTS `cloud`.`swift`;
