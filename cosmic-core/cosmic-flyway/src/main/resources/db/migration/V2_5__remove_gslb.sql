-- Cleanup unused columns from DB.
ALTER TABLE `external_load_balancer_devices`
  DROP COLUMN `is_gslb_provider`,
  DROP COLUMN `is_exclusive_gslb_provider`,
  DROP COLUMN `gslb_site_publicip`,
  DROP COLUMN `gslb_site_privateip`;

ALTER TABLE `region`
  DROP COLUMN `gslb_service_enabled`;
