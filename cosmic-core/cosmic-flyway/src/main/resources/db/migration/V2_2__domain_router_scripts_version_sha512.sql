-- Adjust column `scripts_version` to accommodate SHA512 hashes
ALTER TABLE `domain_router` MODIFY `scripts_version` VARCHAR(128) DEFAULT NULL COMMENT 'scripts version';
