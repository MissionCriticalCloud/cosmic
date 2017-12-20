-- Cleanup unused tables from DB.
DROP TABLE IF EXISTS network_external_lb_device_map;
DROP TABLE IF EXISTS external_load_balancer_devices;

DROP TABLE IF EXISTS global_load_balancer_lb_rule_map;
DROP TABLE IF EXISTS global_load_balancing_rules;
DROP TABLE IF EXISTS inline_load_balancer_nic_map;

-- Remove all autoscaling tables
DROP TABLE IF EXISTS autoscale_policy_condition_map;
DROP TABLE IF EXISTS autoscale_vmgroup_details;
DROP TABLE IF EXISTS autoscale_vmgroup_policy_map;
DROP TABLE IF EXISTS autoscale_vmgroup_vm_map;
DROP TABLE IF EXISTS autoscale_vmgroups;
DROP TABLE IF EXISTS autoscale_vmprofile_details;
DROP TABLE IF EXISTS autoscale_vmprofiles;
DROP TABLE IF EXISTS autoscale_policies;

DROP VIEW IF EXISTS async_job_view;
CREATE VIEW `async_job_view` AS
  SELECT
    `account`.`id`                   AS `account_id`,
    `account`.`uuid`                 AS `account_uuid`,
    `account`.`account_name`         AS `account_name`,
    `account`.`type`                 AS `account_type`,
    `domain`.`id`                    AS `domain_id`,
    `domain`.`uuid`                  AS `domain_uuid`,
    `domain`.`name`                  AS `domain_name`,
    `domain`.`path`                  AS `domain_path`,
    `user`.`id`                      AS `user_id`,
    `user`.`uuid`                    AS `user_uuid`,
    `async_job`.`id`                 AS `id`,
    `async_job`.`uuid`               AS `uuid`,
    `async_job`.`job_cmd`            AS `job_cmd`,
    `async_job`.`job_status`         AS `job_status`,
    `async_job`.`job_process_status` AS `job_process_status`,
    `async_job`.`job_result_code`    AS `job_result_code`,
    `async_job`.`job_result`         AS `job_result`,
    `async_job`.`created`            AS `created`,
    `async_job`.`removed`            AS `removed`,
    `async_job`.`instance_type`      AS `instance_type`,
    `async_job`.`instance_id`        AS `instance_id`,
    (CASE WHEN (`async_job`.`instance_type` = 'Volume')
      THEN `volumes`.`uuid`
     WHEN ((`async_job`.`instance_type` = 'Template') OR (`async_job`.`instance_type` = 'Iso'))
       THEN `vm_template`.`uuid`
     WHEN ((`async_job`.`instance_type` = 'VirtualMachine') OR (`async_job`.`instance_type` = 'ConsoleProxy') OR (`async_job`.`instance_type` = 'SystemVm') OR
           (`async_job`.`instance_type` = 'DomainRouter'))
       THEN `vm_instance`.`uuid`
     WHEN (`async_job`.`instance_type` = 'Snapshot')
       THEN `snapshots`.`uuid`
     WHEN (`async_job`.`instance_type` = 'Host')
       THEN `host`.`uuid`
     WHEN (`async_job`.`instance_type` = 'StoragePool')
       THEN `storage_pool`.`uuid`
     WHEN (`async_job`.`instance_type` = 'IpAddress')
       THEN `user_ip_address`.`uuid`
     WHEN (`async_job`.`instance_type` = 'SecurityGroup')
       THEN `security_group`.`uuid`
     WHEN (`async_job`.`instance_type` = 'PhysicalNetwork')
       THEN `physical_network`.`uuid`
     WHEN (`async_job`.`instance_type` = 'TrafficType')
       THEN `physical_network_traffic_types`.`uuid`
     WHEN (`async_job`.`instance_type` = 'PhysicalNetworkServiceProvider')
       THEN `physical_network_service_providers`.`uuid`
     WHEN (`async_job`.`instance_type` = 'FirewallRule')
       THEN `firewall_rules`.`uuid`
     WHEN (`async_job`.`instance_type` = 'Account')
       THEN `acct`.`uuid`
     WHEN (`async_job`.`instance_type` = 'User')
       THEN `us`.`uuid`
     WHEN (`async_job`.`instance_type` = 'StaticRoute')
       THEN `static_routes`.`uuid`
     WHEN (`async_job`.`instance_type` = 'PrivateGateway')
       THEN `vpc_gateways`.`uuid`
     WHEN (`async_job`.`instance_type` = 'Counter')
       THEN `counter`.`uuid`
     WHEN (`async_job`.`instance_type` = 'Condition')
       THEN `conditions`.`uuid`
     ELSE NULL END)                  AS `instance_uuid`
  FROM (((((((((((((((((((((`async_job`
    LEFT JOIN `account` ON ((`async_job`.`account_id` = `account`.`id`))) LEFT JOIN `domain` ON ((`domain`.`id` = `account`.`domain_id`))) LEFT JOIN `user`
      ON ((`async_job`.`user_id` = `user`.`id`))) LEFT JOIN `volumes` ON ((`async_job`.`instance_id` = `volumes`.`id`))) LEFT JOIN `vm_template`
      ON ((`async_job`.`instance_id` = `vm_template`.`id`))) LEFT JOIN `vm_instance` ON ((`async_job`.`instance_id` = `vm_instance`.`id`))) LEFT JOIN `snapshots`
      ON ((`async_job`.`instance_id` = `snapshots`.`id`))) LEFT JOIN `host` ON ((`async_job`.`instance_id` = `host`.`id`))) LEFT JOIN `storage_pool`
      ON ((`async_job`.`instance_id` = `storage_pool`.`id`))) LEFT JOIN `user_ip_address` ON ((`async_job`.`instance_id` = `user_ip_address`.`id`))) LEFT JOIN `security_group`
      ON ((`async_job`.`instance_id` = `security_group`.`id`))) LEFT JOIN `physical_network` ON ((`async_job`.`instance_id` = `physical_network`.`id`))) LEFT JOIN `physical_network_traffic_types`
      ON ((`async_job`.`instance_id` = `physical_network_traffic_types`.`id`))) LEFT JOIN `physical_network_service_providers`
      ON ((`async_job`.`instance_id` = `physical_network_service_providers`.`id`))) LEFT JOIN `firewall_rules` ON ((`async_job`.`instance_id` = `firewall_rules`.`id`))) LEFT JOIN `account` `acct`
      ON ((`async_job`.`instance_id` = `acct`.`id`))) LEFT JOIN `user` `us` ON ((`async_job`.`instance_id` = `us`.`id`))) LEFT JOIN `static_routes`
      ON ((`async_job`.`instance_id` = `static_routes`.`id`))) LEFT JOIN `vpc_gateways` ON ((`async_job`.`instance_id` = `vpc_gateways`.`id`))) LEFT JOIN `counter`
      ON ((`async_job`.`instance_id` = `counter`.`id`))) LEFT JOIN `conditions` ON ((`async_job`.`instance_id` = `conditions`.`id`)));
