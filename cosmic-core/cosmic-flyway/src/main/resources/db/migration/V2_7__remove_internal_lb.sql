-- Cleanup unused columns from DB.
ALTER TABLE `load_balancing_rules`
  DROP COLUMN `source_ip_address_network_id`,
  DROP COLUMN `source_ip_address`;

ALTER TABLE `network_offerings`
  DROP COLUMN `internal_lb`;
