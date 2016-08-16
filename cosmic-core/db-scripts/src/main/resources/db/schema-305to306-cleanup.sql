#Schema cleanup from 3.0.5 to 3.0.6;


DELETE FROM `cloud`.`configuration` where `cloud`.`configuration`.`name`="vm.hostname.flag";
DELETE FROM `cloud`.`storage_pool_host_ref` WHERE `cloud`.`storage_pool_host_ref`.`pool_id` IN (SELECT `cloud`.`storage_pool`.`id` FROM `cloud`.`storage_pool` WHERE `cloud`.`storage_pool`.`removed` IS NOT NULL);

ALTER TABLE `cloud`.`sync_queue` DROP COLUMN queue_proc_msid;
ALTER TABLE `cloud`.`sync_queue` DROP COLUMN queue_proc_time;
