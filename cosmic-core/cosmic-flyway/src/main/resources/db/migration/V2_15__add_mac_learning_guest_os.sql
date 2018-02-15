-- Add mac_learning column to guest_os
ALTER TABLE `guest_os`
  ADD COLUMN `mac_learning` BOOLEAN NOT NULL DEFAULT FALSE;
