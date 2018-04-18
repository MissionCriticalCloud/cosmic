-- Add disk_controller to volumes table
ALTER TABLE volumes
  ADD COLUMN `disk_controller` VARCHAR(25) NOT NULL DEFAULT 'VIRTIO';

-- Set SCSI based volumes
UPDATE `volumes`
SET `disk_controller` = 'SCSI'
WHERE (`instance_id` IN (SELECT `id`
                         FROM `vm_instance`
                         WHERE (`guest_os_id` IN (SELECT `id`
                                                  FROM `guest_os`
                                                  WHERE `display_name` like '%SCSI%') AND `removed` is null)));

-- Set IDE based volumes
UPDATE `volumes`
SET `disk_controller` = 'IDE'
WHERE (`instance_id` IN (SELECT `id`
                         FROM `vm_instance`
                         WHERE (`guest_os_id` IN (SELECT `id`
                                                  FROM `guest_os`
                                                  WHERE `display_name` like '%Non-VirtIO%') AND `removed` is null)));

-- Update the view
DROP VIEW IF EXISTS `volume_view`;
CREATE VIEW `volume_view` AS
  SELECT
    `cloud`.`volumes`.`id`                      AS `id`,
    `cloud`.`volumes`.`uuid`                    AS `uuid`,
    `cloud`.`volumes`.`name`                    AS `name`,
    `cloud`.`volumes`.`device_id`               AS `device_id`,
    `cloud`.`volumes`.`volume_type`             AS `volume_type`,
    `cloud`.`volumes`.`provisioning_type`       AS `provisioning_type`,
    `cloud`.`volumes`.`size`                    AS `size`,
    `cloud`.`volumes`.`min_iops`                AS `min_iops`,
    `cloud`.`volumes`.`max_iops`                AS `max_iops`,
    `cloud`.`volumes`.`created`                 AS `created`,
    `cloud`.`volumes`.`state`                   AS `state`,
    `cloud`.`volumes`.`attached`                AS `attached`,
    `cloud`.`volumes`.`removed`                 AS `removed`,
    `cloud`.`volumes`.`pod_id`                  AS `pod_id`,
    `cloud`.`volumes`.`display_volume`          AS `display_volume`,
    `cloud`.`volumes`.`format`                  AS `format`,
    `cloud`.`volumes`.`path`                    AS `path`,
    `cloud`.`volumes`.`chain_info`              AS `chain_info`,
    `cloud`.`volumes`.`disk_controller`         AS `disk_controller`,
    `cloud`.`account`.`id`                      AS `account_id`,
    `cloud`.`account`.`uuid`                    AS `account_uuid`,
    `cloud`.`account`.`account_name`            AS `account_name`,
    `cloud`.`account`.`type`                    AS `account_type`,
    `cloud`.`domain`.`id`                       AS `domain_id`,
    `cloud`.`domain`.`uuid`                     AS `domain_uuid`,
    `cloud`.`domain`.`name`                     AS `domain_name`,
    `cloud`.`domain`.`path`                     AS `domain_path`,
    `cloud`.`projects`.`id`                     AS `project_id`,
    `cloud`.`projects`.`uuid`                   AS `project_uuid`,
    `cloud`.`projects`.`name`                   AS `project_name`,
    `cloud`.`data_center`.`id`                  AS `data_center_id`,
    `cloud`.`data_center`.`uuid`                AS `data_center_uuid`,
    `cloud`.`data_center`.`name`                AS `data_center_name`,
    `cloud`.`data_center`.`networktype`         AS `data_center_type`,
    `cloud`.`vm_instance`.`id`                  AS `vm_id`,
    `cloud`.`vm_instance`.`uuid`                AS `vm_uuid`,
    `cloud`.`vm_instance`.`name`                AS `vm_name`,
    `cloud`.`vm_instance`.`state`               AS `vm_state`,
    `cloud`.`vm_instance`.`vm_type`             AS `vm_type`,
    `cloud`.`user_vm`.`display_name`            AS `vm_display_name`,
    `cloud`.`volume_store_ref`.`size`           AS `volume_store_size`,
    `cloud`.`volume_store_ref`.`download_pct`   AS `download_pct`,
    `cloud`.`volume_store_ref`.`download_state` AS `download_state`,
    `cloud`.`volume_store_ref`.`error_str`      AS `error_str`,
    `cloud`.`volume_store_ref`.`created`        AS `created_on_store`,
    `cloud`.`disk_offering`.`id`                AS `disk_offering_id`,
    `cloud`.`disk_offering`.`uuid`              AS `disk_offering_uuid`,
    `cloud`.`disk_offering`.`name`              AS `disk_offering_name`,
    `cloud`.`disk_offering`.`display_text`      AS `disk_offering_display_text`,
    `cloud`.`disk_offering`.`use_local_storage` AS `use_local_storage`,
    `cloud`.`disk_offering`.`system_use`        AS `system_use`,
    `cloud`.`disk_offering`.`bytes_read_rate`   AS `bytes_read_rate`,
    `cloud`.`disk_offering`.`bytes_write_rate`  AS `bytes_write_rate`,
    `cloud`.`disk_offering`.`iops_read_rate`    AS `iops_read_rate`,
    `cloud`.`disk_offering`.`iops_write_rate`   AS `iops_write_rate`,
    `cloud`.`disk_offering`.`cache_mode`        AS `cache_mode`,
    `cloud`.`storage_pool`.`id`                 AS `pool_id`,
    `cloud`.`storage_pool`.`uuid`               AS `pool_uuid`,
    `cloud`.`storage_pool`.`name`               AS `pool_name`,
    `cloud`.`cluster`.`hypervisor_type`         AS `hypervisor_type`,
    `cloud`.`vm_template`.`id`                  AS `template_id`,
    `cloud`.`vm_template`.`uuid`                AS `template_uuid`,
    `cloud`.`vm_template`.`extractable`         AS `extractable`,
    `cloud`.`vm_template`.`type`                AS `template_type`,
    `cloud`.`vm_template`.`name`                AS `template_name`,
    `cloud`.`vm_template`.`display_text`        AS `template_display_text`,
    `iso`.`id`                                  AS `iso_id`,
    `iso`.`uuid`                                AS `iso_uuid`,
    `iso`.`name`                                AS `iso_name`,
    `iso`.`display_text`                        AS `iso_display_text`,
    `cloud`.`resource_tags`.`id`                AS `tag_id`,
    `cloud`.`resource_tags`.`uuid`              AS `tag_uuid`,
    `cloud`.`resource_tags`.`key`               AS `tag_key`,
    `cloud`.`resource_tags`.`value`             AS `tag_value`,
    `cloud`.`resource_tags`.`domain_id`         AS `tag_domain_id`,
    `cloud`.`domain`.`uuid`                     AS `tag_domain_uuid`,
    `cloud`.`domain`.`name`                     AS `tag_domain_name`,
    `cloud`.`resource_tags`.`account_id`        AS `tag_account_id`,
    `cloud`.`account`.`account_name`            AS `tag_account_name`,
    `cloud`.`resource_tags`.`resource_id`       AS `tag_resource_id`,
    `cloud`.`resource_tags`.`resource_uuid`     AS `tag_resource_uuid`,
    `cloud`.`resource_tags`.`resource_type`     AS `tag_resource_type`,
    `cloud`.`resource_tags`.`customer`          AS `tag_customer`,
    `cloud`.`async_job`.`id`                    AS `job_id`,
    `cloud`.`async_job`.`uuid`                  AS `job_uuid`,
    `cloud`.`async_job`.`job_status`            AS `job_status`,
    `cloud`.`async_job`.`account_id`            AS `job_account_id`
  FROM ((((((((((((((`cloud`.`volumes`
    JOIN `cloud`.`account` ON ((`cloud`.`volumes`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain` ON ((`cloud`.`volumes`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN
    `cloud`.`projects` ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`account`.`id`))) LEFT JOIN `cloud`.`data_center`
      ON ((`cloud`.`volumes`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN `cloud`.`vm_instance` ON ((`cloud`.`volumes`.`instance_id` = `cloud`.`vm_instance`.`id`))) LEFT JOIN
    `cloud`.`user_vm` ON ((`cloud`.`user_vm`.`id` = `cloud`.`vm_instance`.`id`))) LEFT JOIN `cloud`.`volume_store_ref` ON ((`cloud`.`volumes`.`id` = `cloud`.`volume_store_ref`.`volume_id`))) LEFT JOIN
    `cloud`.`disk_offering` ON ((`cloud`.`volumes`.`disk_offering_id` = `cloud`.`disk_offering`.`id`))) LEFT JOIN `cloud`.`storage_pool`
      ON ((`cloud`.`volumes`.`pool_id` = `cloud`.`storage_pool`.`id`))) LEFT JOIN `cloud`.`cluster` ON ((`cloud`.`storage_pool`.`cluster_id` = `cloud`.`cluster`.`id`))) LEFT JOIN `cloud`.`vm_template`
      ON ((`cloud`.`volumes`.`template_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN `cloud`.`vm_template` `iso` ON ((`iso`.`id` = `cloud`.`volumes`.`iso_id`))) LEFT JOIN `cloud`.`resource_tags`
      ON (((`cloud`.`resource_tags`.`resource_id` = `cloud`.`volumes`.`id`) AND (`cloud`.`resource_tags`.`resource_type` = 'Volume')))) LEFT JOIN `cloud`.`async_job`
      ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`volumes`.`id`) AND (`cloud`.`async_job`.`instance_type` = 'Volume') AND (`cloud`.`async_job`.`job_status` = 0))));
