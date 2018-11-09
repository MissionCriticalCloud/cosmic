-- Recreate domain_router_view
DROP VIEW IF EXISTS `domain_router_view`;
CREATE VIEW `domain_router_view` AS
  SELECT
    `cloud`.`vm_instance`.`id` AS `id`,
    `cloud`.`vm_instance`.`name` AS `name`,
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
    `cloud`.`vm_instance`.`uuid` AS `uuid`,
    `cloud`.`vm_instance`.`created` AS `created`,
    `cloud`.`vm_instance`.`state` AS `state`,
    `cloud`.`vm_instance`.`removed` AS `removed`,
    `cloud`.`vm_instance`.`pod_id` AS `pod_id`,
    `cloud`.`vm_instance`.`instance_name` AS `instance_name`,
    `cloud`.`vm_instance`.`last_start_datetime` AS `last_start_datetime`,
    `cloud`.`vm_instance`.`last_start_version` AS `last_start_version`,
    `cloud`.`host_pod_ref`.`uuid` AS `pod_uuid`,
    `cloud`.`data_center`.`id` AS `data_center_id`,
    `cloud`.`data_center`.`uuid` AS `data_center_uuid`,
    `cloud`.`data_center`.`name` AS `data_center_name`,
    `cloud`.`data_center`.`networktype` AS `data_center_type`,
    `cloud`.`data_center`.`dns1` AS `dns1`,
    `cloud`.`data_center`.`dns2` AS `dns2`,
    `cloud`.`data_center`.`ip6_dns1` AS `ip6_dns1`,
    `cloud`.`data_center`.`ip6_dns2` AS `ip6_dns2`,
    `cloud`.`host`.`id` AS `host_id`,
    `cloud`.`host`.`uuid` AS `host_uuid`,
    `cloud`.`host`.`name` AS `host_name`,
    `cloud`.`host`.`hypervisor_type` AS `hypervisor_type`,
    `cloud`.`host`.`cluster_id` AS `cluster_id`,
    `cloud`.`vm_template`.`id` AS `template_id`,
    `cloud`.`vm_template`.`uuid` AS `template_uuid`,
    `cloud`.`service_offering`.`id` AS `service_offering_id`,
    `cloud`.`disk_offering`.`uuid` AS `service_offering_uuid`,
    `cloud`.`disk_offering`.`name` AS `service_offering_name`,
    `cloud`.`nics`.`id` AS `nic_id`,
    `cloud`.`nics`.`uuid` AS `nic_uuid`,
    `cloud`.`nics`.`network_id` AS `network_id`,
    `cloud`.`nics`.`ip4_address` AS `ip_address`,
    `cloud`.`nics`.`ip6_address` AS `ip6_address`,
    `cloud`.`nics`.`ip6_gateway` AS `ip6_gateway`,
    `cloud`.`nics`.`ip6_cidr` AS `ip6_cidr`,
    `cloud`.`nics`.`default_nic` AS `is_default_nic`,
    `cloud`.`nics`.`gateway` AS `gateway`,
    `cloud`.`nics`.`netmask` AS `netmask`,
    `cloud`.`nics`.`mac_address` AS `mac_address`,
    `cloud`.`nics`.`broadcast_uri` AS `broadcast_uri`,
    `cloud`.`nics`.`isolation_uri` AS `isolation_uri`,
    `cloud`.`vpc`.`id` AS `vpc_id`,
    `cloud`.`vpc`.`uuid` AS `vpc_uuid`,
    `cloud`.`vpc`.`name` AS `vpc_name`,
    `cloud`.`networks`.`uuid` AS `network_uuid`,
    `cloud`.`networks`.`name` AS `network_name`,
    `cloud`.`networks`.`network_domain` AS `network_domain`,
    `cloud`.`networks`.`traffic_type` AS `traffic_type`,
    `cloud`.`networks`.`guest_type` AS `guest_type`,
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`,
    `cloud`.`domain_router`.`template_version` AS `template_version`,
    `cloud`.`domain_router`.`scripts_version` AS `scripts_version`,
    `cloud`.`domain_router`.`is_redundant_router` AS `is_redundant_router`,
    `cloud`.`domain_router`.`redundant_state` AS `redundant_state`,
    `cloud`.`domain_router`.`stop_pending` AS `stop_pending`,
    `cloud`.`domain_router`.`role` AS `role`
  FROM ((((((((((((((`cloud`.`domain_router`
    JOIN `cloud`.`vm_instance` ON ((`cloud`.`vm_instance`.`id` = `cloud`.`domain_router`.`id`))) JOIN `cloud`.`account`
      ON ((`cloud`.`vm_instance`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`vm_instance`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`host_pod_ref`
      ON ((`cloud`.`vm_instance`.`pod_id` = `cloud`.`host_pod_ref`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`account`.`id`))) LEFT JOIN `cloud`.`data_center`
      ON ((`cloud`.`vm_instance`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN `cloud`.`host`
      ON ((`cloud`.`vm_instance`.`host_id` = `cloud`.`host`.`id`))) LEFT JOIN `cloud`.`vm_template`
      ON ((`cloud`.`vm_instance`.`vm_template_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN `cloud`.`service_offering`
      ON ((`cloud`.`vm_instance`.`service_offering_id` = `cloud`.`service_offering`.`id`))) LEFT JOIN
    `cloud`.`disk_offering` ON ((`cloud`.`vm_instance`.`service_offering_id` = `cloud`.`disk_offering`.`id`))) LEFT JOIN
    `cloud`.`nics`
      ON (((`cloud`.`vm_instance`.`id` = `cloud`.`nics`.`instance_id`) AND isnull(`cloud`.`nics`.`removed`)))) LEFT JOIN
    `cloud`.`networks` ON ((`cloud`.`nics`.`network_id` = `cloud`.`networks`.`id`))) LEFT JOIN `cloud`.`vpc`
      ON (((`cloud`.`domain_router`.`vpc_id` = `cloud`.`vpc`.`id`) AND isnull(`cloud`.`vpc`.`removed`)))) LEFT JOIN
    `cloud`.`async_job` ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`vm_instance`.`id`) AND
                             (`cloud`.`async_job`.`instance_type` = 'DomainRouter') AND
                             (`cloud`.`async_job`.`job_status` = 0))));