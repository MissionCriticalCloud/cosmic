-- Rename unused colums vpc_offerings table
ALTER TABLE `vpc_offerings`
  DROP COLUMN `supports_distributed_router`,
  DROP COLUMN `supports_region_level_vpc`;
