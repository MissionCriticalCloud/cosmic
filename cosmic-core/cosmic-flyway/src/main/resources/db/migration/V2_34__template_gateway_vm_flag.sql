ALTER TABLE vm_template
  ADD COLUMN `is_remote_gateway_template` INT(1) UNSIGNED DEFAULT '0' NOT NULL;

-- template
DROP VIEW IF EXISTS `template_view`;
CREATE VIEW `template_view` AS
  SELECT
    `cloud`.`vm_template`.`id` AS `id`,
    `cloud`.`vm_template`.`uuid` AS `uuid`,
    `cloud`.`vm_template`.`unique_name` AS `unique_name`,
    `cloud`.`vm_template`.`name` AS `name`,
    `cloud`.`vm_template`.`public` AS `public`,
    `cloud`.`vm_template`.`featured` AS `featured`,
    `cloud`.`vm_template`.`type` AS `type`,
    `cloud`.`vm_template`.`bits` AS `bits`,
    `cloud`.`vm_template`.`url` AS `url`,
    `cloud`.`vm_template`.`format` AS `format`,
    `cloud`.`vm_template`.`created` AS `created`,
    `cloud`.`vm_template`.`checksum` AS `checksum`,
    `cloud`.`vm_template`.`display_text` AS `display_text`,
    `cloud`.`vm_template`.`enable_password` AS `enable_password`,
    `cloud`.`vm_template`.`dynamically_scalable` AS `dynamically_scalable`,
    `cloud`.`vm_template`.`state` AS `template_state`,
    `cloud`.`vm_template`.`guest_os_id` AS `guest_os_id`,
    `cloud`.`guest_os`.`uuid` AS `guest_os_uuid`,
    `cloud`.`guest_os`.`display_name` AS `guest_os_name`,
    `cloud`.`vm_template`.`bootable` AS `bootable`,
    `cloud`.`vm_template`.`prepopulate` AS `prepopulate`,
    `cloud`.`vm_template`.`cross_zones` AS `cross_zones`,
    `cloud`.`vm_template`.`hypervisor_type` AS `hypervisor_type`,
    `cloud`.`vm_template`.`extractable` AS `extractable`,
    `cloud`.`vm_template`.`template_tag` AS `template_tag`,
    `cloud`.`vm_template`.`sort_key` AS `sort_key`,
    `cloud`.`vm_template`.`removed` AS `removed`,
    `cloud`.`vm_template`.`enable_sshkey` AS `enable_sshkey`,
    `cloud`.`vm_template`.`optimise_for` AS `optimise_for`,
    `cloud`.`vm_template`.`manufacturer_string` AS `manufacturer_string`,
    `cloud`.`vm_template`.`mac_learning` AS `mac_learning`,
    `cloud`.`vm_template`.`cpu_flags` AS `cpu_flags`,
    `cloud`.`vm_template`.`maintenance_policy` AS `maintenance_policy`,
    `cloud`.`vm_template`.`is_remote_gateway_template` AS `is_remote_gateway_template`,
    `source_template`.`id` AS `source_template_id`,
    `source_template`.`uuid` AS `source_template_uuid`,
    `cloud`.`account`.`id` AS `account_id`,
    `cloud`.`account`.`uuid` AS `account_uuid`,
    `cloud`.`account`.`account_name` AS `account_name`,
    `cloud`.`account`.`type` AS `account_type`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`,
    `cloud`.`projects`.`id` AS `project_id`,
    `cloud`.`projects`.`uuid` AS `project_uuid`,
    `cloud`.`projects`.`name` AS `project_name`,
    `cloud`.`data_center`.`id` AS `data_center_id`,
    `cloud`.`data_center`.`uuid` AS `data_center_uuid`,
    `cloud`.`data_center`.`name` AS `data_center_name`,
    `cloud`.`launch_permission`.`account_id` AS `lp_account_id`,
    `cloud`.`template_store_ref`.`store_id` AS `store_id`,
    `cloud`.`image_store`.`scope` AS `store_scope`,
    `cloud`.`template_store_ref`.`state` AS `state`,
    `cloud`.`template_store_ref`.`download_state` AS `download_state`,
    `cloud`.`template_store_ref`.`download_pct` AS `download_pct`,
    `cloud`.`template_store_ref`.`error_str` AS `error_str`,
    `cloud`.`template_store_ref`.`size` AS `size`,
    `cloud`.`template_store_ref`.`destroyed` AS `destroyed`,
    `cloud`.`template_store_ref`.`created` AS `created_on_store`,
    `cloud`.`vm_template_details`.`name` AS `detail_name`,
    `cloud`.`vm_template_details`.`value` AS `detail_value`,
    `cloud`.`resource_tags`.`id` AS `tag_id`,
    `cloud`.`resource_tags`.`uuid` AS `tag_uuid`,
    `cloud`.`resource_tags`.`key` AS `tag_key`,
    `cloud`.`resource_tags`.`value` AS `tag_value`,
    `cloud`.`resource_tags`.`domain_id` AS `tag_domain_id`,
    `cloud`.`domain`.`uuid` AS `tag_domain_uuid`,
    `cloud`.`domain`.`name` AS `tag_domain_name`,
    `cloud`.`resource_tags`.`account_id` AS `tag_account_id`,
    `cloud`.`account`.`account_name` AS `tag_account_name`,
    `cloud`.`resource_tags`.`resource_id` AS `tag_resource_id`,
    `cloud`.`resource_tags`.`resource_uuid` AS `tag_resource_uuid`,
    `cloud`.`resource_tags`.`resource_type` AS `tag_resource_type`,
    `cloud`.`resource_tags`.`customer` AS `tag_customer`,
    concat(`cloud`.`vm_template`.`id`, '_', ifnull(`cloud`.`data_center`.`id`, 0)) AS `temp_zone_pair`
  FROM ((((((((((((`cloud`.`vm_template`
    JOIN `cloud`.`guest_os` ON ((`cloud`.`guest_os`.`id` = `cloud`.`vm_template`.`guest_os_id`))) JOIN `cloud`.`account`
      ON ((`cloud`.`account`.`id` = `cloud`.`vm_template`.`account_id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`domain`.`id` = `cloud`.`account`.`domain_id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`account`.`id`))) LEFT JOIN `cloud`.`vm_template_details`
      ON ((`cloud`.`vm_template_details`.`template_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN
    `cloud`.`vm_template` `source_template`
      ON ((`source_template`.`id` = `cloud`.`vm_template`.`source_template_id`))) LEFT JOIN `cloud`.`template_store_ref`
      ON (((`cloud`.`template_store_ref`.`template_id` = `cloud`.`vm_template`.`id`) AND
           (`cloud`.`template_store_ref`.`store_role` = 'Image') AND
           (`cloud`.`template_store_ref`.`destroyed` = 0)))) LEFT JOIN `cloud`.`image_store`
      ON ((isnull(`cloud`.`image_store`.`removed`) AND (`cloud`.`template_store_ref`.`store_id` IS NOT NULL) AND
           (`cloud`.`image_store`.`id` = `cloud`.`template_store_ref`.`store_id`)))) LEFT JOIN
    `cloud`.`template_zone_ref` ON (((`cloud`.`template_zone_ref`.`template_id` = `cloud`.`vm_template`.`id`) AND
                                     isnull(`cloud`.`template_store_ref`.`store_id`) AND
                                     isnull(`cloud`.`template_zone_ref`.`removed`)))) LEFT JOIN `cloud`.`data_center`
      ON (((`cloud`.`image_store`.`data_center_id` = `cloud`.`data_center`.`id`) OR
           (`cloud`.`template_zone_ref`.`zone_id` = `cloud`.`data_center`.`id`)))) LEFT JOIN `cloud`.`launch_permission`
      ON ((`cloud`.`launch_permission`.`template_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN `cloud`.`resource_tags`
      ON (((`cloud`.`resource_tags`.`resource_id` = `cloud`.`vm_template`.`id`) AND
           ((`cloud`.`resource_tags`.`resource_type` = 'Template') OR
            (`cloud`.`resource_tags`.`resource_type` = 'ISO')))));
