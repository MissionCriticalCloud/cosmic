-- Add maintenance_policy to vm_instance table
ALTER TABLE vm_instance
  ADD COLUMN `boot_order` varchar(65) DEFAULT 'cdrom,hd,network' COMMENT 'Boot order priority';

-- Recreate user_vm_view
DROP VIEW IF EXISTS `user_vm_view`;
CREATE VIEW `user_vm_view` AS
SELECT
    `cloud`.`vm_instance`.`id`                       AS `id`,
    `cloud`.`vm_instance`.`name`                     AS `name`,
    `cloud`.`user_vm`.`display_name`                 AS `display_name`,
    `cloud`.`user_vm`.`user_data`                    AS `user_data`,
    `cloud`.`account`.`id`                           AS `account_id`,
    `cloud`.`account`.`uuid`                         AS `account_uuid`,
    `cloud`.`account`.`account_name`                 AS `account_name`,
    `cloud`.`account`.`type`                         AS `account_type`,
    `cloud`.`domain`.`id`                            AS `domain_id`,
    `cloud`.`domain`.`uuid`                          AS `domain_uuid`,
    `cloud`.`domain`.`name`                          AS `domain_name`,
    `cloud`.`domain`.`path`                          AS `domain_path`,
    `cloud`.`projects`.`id`                          AS `project_id`,
    `cloud`.`projects`.`uuid`                        AS `project_uuid`,
    `cloud`.`projects`.`name`                        AS `project_name`,
    `cloud`.`instance_group`.`id`                    AS `instance_group_id`,
    `cloud`.`instance_group`.`uuid`                  AS `instance_group_uuid`,
    `cloud`.`instance_group`.`name`                  AS `instance_group_name`,
    `cloud`.`vm_instance`.`uuid`                     AS `uuid`,
    `cloud`.`vm_instance`.`user_id`                  AS `user_id`,
    `cloud`.`vm_instance`.`last_host_id`             AS `last_host_id`,
    `cloud`.`vm_instance`.`vm_type`                  AS `type`,
    `cloud`.`vm_instance`.`limit_cpu_use`            AS `limit_cpu_use`,
    `cloud`.`vm_instance`.`created`                  AS `created`,
    `cloud`.`vm_instance`.`state`                    AS `state`,
    `cloud`.`vm_instance`.`removed`                  AS `removed`,
    `cloud`.`vm_instance`.`ha_enabled`               AS `ha_enabled`,
    `cloud`.`vm_instance`.`hypervisor_type`          AS `hypervisor_type`,
    `cloud`.`vm_instance`.`instance_name`            AS `instance_name`,
    `cloud`.`vm_instance`.`guest_os_id`              AS `guest_os_id`,
    `cloud`.`vm_instance`.`display_vm`               AS `display_vm`,
    `cloud`.`guest_os`.`uuid`                        AS `guest_os_uuid`,
    `cloud`.`vm_instance`.`pod_id`                   AS `pod_id`,
    `cloud`.`host_pod_ref`.`uuid`                    AS `pod_uuid`,
    `cloud`.`vm_instance`.`private_ip_address`       AS `private_ip_address`,
    `cloud`.`vm_instance`.`private_mac_address`      AS `private_mac_address`,
    `cloud`.`vm_instance`.`vm_type`                  AS `vm_type`,
    `cloud`.`data_center`.`id`                       AS `data_center_id`,
    `cloud`.`data_center`.`uuid`                     AS `data_center_uuid`,
    `cloud`.`data_center`.`name`                     AS `data_center_name`,
    `cloud`.`data_center`.`networktype`              AS `data_center_type`,
    `cloud`.`host`.`id`                              AS `host_id`,
    `cloud`.`host`.`uuid`                            AS `host_uuid`,
    `cloud`.`host`.`name`                            AS `host_name`,
    `cloud`.`vm_template`.`id`                       AS `template_id`,
    `cloud`.`vm_template`.`uuid`                     AS `template_uuid`,
    `cloud`.`vm_template`.`name`                     AS `template_name`,
    `cloud`.`vm_template`.`display_text`             AS `template_display_text`,
    `cloud`.`vm_template`.`enable_password`          AS `password_enabled`,
    `iso`.`id`                                       AS `iso_id`,
    `iso`.`uuid`                                     AS `iso_uuid`,
    `iso`.`name`                                     AS `iso_name`,
    `iso`.`display_text`                             AS `iso_display_text`,
    `cloud`.`service_offering`.`id`                  AS `service_offering_id`,
    `svc_disk_offering`.`uuid`                       AS `service_offering_uuid`,
    `cloud`.`disk_offering`.`uuid`                   AS `disk_offering_uuid`,
    `cloud`.`disk_offering`.`id`                     AS `disk_offering_id`,
    (CASE WHEN isnull(`cloud`.`service_offering`.`cpu`)
              THEN `custom_cpu`.`value`
          ELSE `cloud`.`service_offering`.`cpu` END)      AS `cpu`,
    (CASE WHEN isnull(`cloud`.`service_offering`.`ram_size`)
              THEN `custom_ram_size`.`value`
          ELSE `cloud`.`service_offering`.`ram_size` END) AS `ram_size`,
    `svc_disk_offering`.`name`                       AS `service_offering_name`,
    `cloud`.`disk_offering`.`name`                   AS `disk_offering_name`,
    `cloud`.`storage_pool`.`id`                      AS `pool_id`,
    `cloud`.`storage_pool`.`uuid`                    AS `pool_uuid`,
    `cloud`.`storage_pool`.`pool_type`               AS `pool_type`,
    `cloud`.`volumes`.`id`                           AS `volume_id`,
    `cloud`.`volumes`.`uuid`                         AS `volume_uuid`,
    `cloud`.`volumes`.`device_id`                    AS `volume_device_id`,
    `cloud`.`volumes`.`volume_type`                  AS `volume_type`,
    `cloud`.`volumes`.`disk_controller`              AS `volume_diskcontroller`,
    `cloud`.`nics`.`id`                              AS `nic_id`,
    `cloud`.`nics`.`uuid`                            AS `nic_uuid`,
    `cloud`.`nics`.`network_id`                      AS `network_id`,
    `cloud`.`nics`.`ip4_address`                     AS `ip_address`,
    `cloud`.`nics`.`ip6_address`                     AS `ip6_address`,
    `cloud`.`nics`.`ip6_gateway`                     AS `ip6_gateway`,
    `cloud`.`nics`.`ip6_cidr`                        AS `ip6_cidr`,
    `cloud`.`nics`.`default_nic`                     AS `is_default_nic`,
    `cloud`.`nics`.`gateway`                         AS `gateway`,
    `cloud`.`nics`.`netmask`                         AS `netmask`,
    `cloud`.`nics`.`mac_address`                     AS `mac_address`,
    `cloud`.`nics`.`broadcast_uri`                   AS `broadcast_uri`,
    `cloud`.`nics`.`isolation_uri`                   AS `isolation_uri`,
    `cloud`.`nics`.`mirror_ip_address`               AS `mirror_ip_address`,
    `cloud`.`nics`.`mirror_key`                      AS `mirror_key`,
    `cloud`.`vpc`.`id`                               AS `vpc_id`,
    `cloud`.`vpc`.`uuid`                             AS `vpc_uuid`,
    `cloud`.`networks`.`uuid`                        AS `network_uuid`,
    `cloud`.`networks`.`name`                        AS `network_name`,
    `cloud`.`networks`.`traffic_type`                AS `traffic_type`,
    `cloud`.`networks`.`guest_type`                  AS `guest_type`,
    `cloud`.`user_ip_address`.`id`                   AS `public_ip_id`,
    `cloud`.`user_ip_address`.`uuid`                 AS `public_ip_uuid`,
    `cloud`.`user_ip_address`.`public_ip_address`    AS `public_ip_address`,
    `cloud`.`ssh_keypairs`.`keypair_name`            AS `keypair_name`,
    `cloud`.`resource_tags`.`id`                     AS `tag_id`,
    `cloud`.`resource_tags`.`uuid`                   AS `tag_uuid`,
    `cloud`.`resource_tags`.`key`                    AS `tag_key`,
    `cloud`.`resource_tags`.`value`                  AS `tag_value`,
    `cloud`.`resource_tags`.`domain_id`              AS `tag_domain_id`,
    `cloud`.`domain`.`uuid`                          AS `tag_domain_uuid`,
    `cloud`.`domain`.`name`                          AS `tag_domain_name`,
    `cloud`.`resource_tags`.`account_id`             AS `tag_account_id`,
    `cloud`.`account`.`account_name`                 AS `tag_account_name`,
    `cloud`.`resource_tags`.`resource_id`            AS `tag_resource_id`,
    `cloud`.`resource_tags`.`resource_uuid`          AS `tag_resource_uuid`,
    `cloud`.`resource_tags`.`resource_type`          AS `tag_resource_type`,
    `cloud`.`resource_tags`.`customer`               AS `tag_customer`,
    `cloud`.`async_job`.`id`                         AS `job_id`,
    `cloud`.`async_job`.`uuid`                       AS `job_uuid`,
    `cloud`.`async_job`.`job_status`                 AS `job_status`,
    `cloud`.`async_job`.`account_id`                 AS `job_account_id`,
    `cloud`.`affinity_group`.`id`                    AS `affinity_group_id`,
    `cloud`.`affinity_group`.`uuid`                  AS `affinity_group_uuid`,
    `cloud`.`affinity_group`.`name`                  AS `affinity_group_name`,
    `cloud`.`affinity_group`.`description`           AS `affinity_group_description`,
    `cloud`.`vm_instance`.`dynamically_scalable`     AS `dynamically_scalable`,
    `cloud`.`vm_instance`.`manufacturer_string`      AS `manufacturer_string`,
    `cloud`.`vm_instance`.`optimise_for`             AS `optimise_for`,
    `cloud`.`vm_instance`.`mac_learning`             AS `mac_learning`,
    `cloud`.`vm_instance`.`cpu_flags`                AS `cpu_flags`,
    `cloud`.`vm_instance`.`requires_restart`         AS `requires_restart`,
    `cloud`.`vm_instance`.`maintenance_policy`       AS `maintenance_policy`,
    `cloud`.`vm_instance`.`boot_menu_timeout`        AS `boot_menu_timeout`,
    `cloud`.`vm_instance`.`boot_order`               AS `boot_order`,
    `cloud`.`vm_instance`.`last_start_datetime`      AS `last_start_datetime`,
    `cloud`.`vm_instance`.`last_start_version`       AS `last_start_version`


FROM (((((((((((((((((((((((((((((`cloud`.`user_vm`
    JOIN `cloud`.`vm_instance` ON (((`cloud`.`vm_instance`.`id` = `cloud`.`user_vm`.`id`) AND isnull(`cloud`.`vm_instance`.`removed`)))) JOIN `cloud`.`account`
    ON ((`cloud`.`vm_instance`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain` ON ((`cloud`.`vm_instance`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`guest_os`
    ON ((`cloud`.`vm_instance`.`guest_os_id` = `cloud`.`guest_os`.`id`))) LEFT JOIN `cloud`.`host_pod_ref` ON ((`cloud`.`vm_instance`.`pod_id` = `cloud`.`host_pod_ref`.`id`))) LEFT JOIN
    `cloud`.`projects` ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`account`.`id`))) LEFT JOIN `cloud`.`instance_group_vm_map`
    ON ((`cloud`.`vm_instance`.`id` = `cloud`.`instance_group_vm_map`.`instance_id`))) LEFT JOIN `cloud`.`instance_group`
    ON ((`cloud`.`instance_group_vm_map`.`group_id` = `cloud`.`instance_group`.`id`))) LEFT JOIN `cloud`.`data_center`
    ON ((`cloud`.`vm_instance`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN `cloud`.`host` ON ((`cloud`.`vm_instance`.`host_id` = `cloud`.`host`.`id`))) LEFT JOIN `cloud`.`vm_template`
    ON ((`cloud`.`vm_instance`.`vm_template_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN `cloud`.`vm_template` `iso` ON ((`iso`.`id` = `cloud`.`user_vm`.`iso_id`))) LEFT JOIN
    `cloud`.`service_offering` ON ((`cloud`.`vm_instance`.`service_offering_id` = `cloud`.`service_offering`.`id`))) LEFT JOIN `cloud`.`disk_offering` `svc_disk_offering`
    ON ((`cloud`.`vm_instance`.`service_offering_id` = `svc_disk_offering`.`id`))) LEFT JOIN `cloud`.`disk_offering`
    ON ((`cloud`.`vm_instance`.`disk_offering_id` = `cloud`.`disk_offering`.`id`))) LEFT JOIN `cloud`.`volumes` ON ((`cloud`.`vm_instance`.`id` = `cloud`.`volumes`.`instance_id`))) LEFT JOIN
    `cloud`.`storage_pool` ON ((`cloud`.`volumes`.`pool_id` = `cloud`.`storage_pool`.`id`))) LEFT JOIN `cloud`.`nics`
    ON (((`cloud`.`vm_instance`.`id` = `cloud`.`nics`.`instance_id`) AND isnull(`cloud`.`nics`.`removed`)))) LEFT JOIN `cloud`.`networks`
    ON ((`cloud`.`nics`.`network_id` = `cloud`.`networks`.`id`))) LEFT JOIN `cloud`.`vpc` ON (((`cloud`.`networks`.`vpc_id` = `cloud`.`vpc`.`id`) AND isnull(`cloud`.`vpc`.`removed`)))) LEFT JOIN
    `cloud`.`user_ip_address` ON ((`cloud`.`user_ip_address`.`vm_id` = `cloud`.`vm_instance`.`id`))) LEFT JOIN `cloud`.`user_vm_details` `ssh_details`
    ON (((`ssh_details`.`vm_id` = `cloud`.`vm_instance`.`id`) AND (`ssh_details`.`name` = 'SSH.PublicKey')))) LEFT JOIN `cloud`.`ssh_keypairs`
    ON (((`cloud`.`ssh_keypairs`.`public_key` = `ssh_details`.`value`) AND (`cloud`.`ssh_keypairs`.`account_id` = `cloud`.`account`.`id`)))) LEFT JOIN `cloud`.`resource_tags`
    ON (((`cloud`.`resource_tags`.`resource_id` = `cloud`.`vm_instance`.`id`) AND (`cloud`.`resource_tags`.`resource_type` = 'UserVm')))) LEFT JOIN `cloud`.`async_job`
    ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`vm_instance`.`id`) AND (`cloud`.`async_job`.`instance_type` = 'VirtualMachine') AND (`cloud`.`async_job`.`job_status` = 0)))) LEFT JOIN
    `cloud`.`affinity_group_vm_map` ON ((`cloud`.`vm_instance`.`id` = `cloud`.`affinity_group_vm_map`.`instance_id`))) LEFT JOIN `cloud`.`affinity_group`
    ON ((`cloud`.`affinity_group_vm_map`.`affinity_group_id` = `cloud`.`affinity_group`.`id`))) LEFT JOIN `cloud`.`user_vm_details` `custom_cpu`
    ON (((`custom_cpu`.`vm_id` = `cloud`.`vm_instance`.`id`) AND (`custom_cpu`.`name` = 'CpuNumber')))) LEFT JOIN `cloud`.`user_vm_details` `custom_ram_size`
                                                                                                                  ON (((`custom_ram_size`.`vm_id` = `cloud`.`vm_instance`.`id`) AND (`custom_ram_size`.`name` = 'memory'))));
