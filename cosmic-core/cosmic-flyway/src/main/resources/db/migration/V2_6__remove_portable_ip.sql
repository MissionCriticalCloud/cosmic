-- Cleanup unused columns from DB.
ALTER TABLE `user_ip_address`
  DROP COLUMN `is_portable`;

ALTER TABLE `region`
  DROP COLUMN `portableip_service_enabled`;
