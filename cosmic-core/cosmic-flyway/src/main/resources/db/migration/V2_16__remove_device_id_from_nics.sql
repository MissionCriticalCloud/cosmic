-- Removes the `device_id` column from the `nics` table
ALTER TABLE `nics`
  DROP COLUMN `device_id`;
