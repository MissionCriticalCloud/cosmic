--;
-- Schema cleanup from 5.1.1 to 5.2.0;
--;

-- Remove unused table async_job_journal
DROP TABLE IF EXISTS `cloud`.`async_job_journal`;

-- Remove unused table cluster_vsm_map
DROP TABLE IF EXISTS `cloud`.`cluster_vsm_map`;

-- Remove unused table cmd_exec_log
DROP TABLE IF EXISTS `cloud`.`cmd_exec_log`;

-- Remove unused table elastic_lb_vm_map
DROP TABLE IF EXISTS `cloud`.`elastic_lb_vm_map`;

-- Remove unused table network_asa1000v_map
DROP TABLE IF EXISTS `cloud`.`network_asa1000v_map`;

-- Remove unused table external_cisco_asa1000v_devices
DROP TABLE IF EXISTS `cloud`.`external_cisco_asa1000v_devices`;

-- Remove unused table external_cisco_vnmc_devices
DROP TABLE IF EXISTS `cloud`.`external_cisco_vnmc_devices`;

-- Remove unused table network_external_firewall_device_map
DROP TABLE IF EXISTS `cloud`.`network_external_firewall_device_map`;

-- Remove unused table external_firewall_devices
DROP TABLE IF EXISTS `cloud`.`external_firewall_devices`;

-- Remove duplicated indexes
ALTER TABLE `cloud`.`user_vm` DROP INDEX `id_2` ,DROP INDEX `id` ;
ALTER TABLE `cloud`.`domain_router` DROP INDEX `id_2` ,DROP INDEX `id` ;
ALTER TABLE `cloud`.`vm_instance` DROP INDEX `id_2` ,DROP INDEX `id` ;
ALTER TABLE `cloud`.`account_vlan_map` DROP INDEX `id` ;
ALTER TABLE `cloud`.`account_vnet_map` DROP INDEX `id` ;
ALTER TABLE `cloud`.`cluster` DROP INDEX `id` ;
ALTER TABLE `cloud`.`conditions` DROP INDEX `id` ;
ALTER TABLE `cloud`.`counter` DROP INDEX `id` ;
ALTER TABLE `cloud`.`data_center` DROP INDEX `id` ;
ALTER TABLE `cloud`.`dc_storage_network_ip_range` DROP INDEX `id` ;
ALTER TABLE `cloud`.`dedicated_resources` DROP INDEX `id` ;
ALTER TABLE `cloud`.`host_pod_ref` DROP INDEX `id` ;
ALTER TABLE `cloud`.`image_store_details` DROP INDEX `id` ;
ALTER TABLE `cloud`.`instance_group` DROP INDEX `id` ;
ALTER TABLE `cloud`.`network_acl_item_cidrs` DROP INDEX `id` ;
ALTER TABLE `cloud`.`network_offerings` DROP INDEX `id` ;
ALTER TABLE `cloud`.`nic_secondary_ips` DROP INDEX `id` ;
ALTER TABLE `cloud`.`nics` DROP INDEX `id` ;
ALTER TABLE `cloud`.`op_ha_work` DROP INDEX `id` ;
ALTER TABLE `cloud`.`op_host` DROP INDEX `id` ;
ALTER TABLE `cloud`.`op_host_transfer` DROP INDEX `id` ;
ALTER TABLE `cloud`.`op_networks` DROP INDEX `id` ;
ALTER TABLE `cloud`.`op_nwgrp_work` DROP INDEX `id` ;
ALTER TABLE `cloud`.`op_vm_ruleset_log` DROP INDEX `id` ;
ALTER TABLE `cloud`.`op_vpc_distributed_router_sequence_no` DROP INDEX `id` ;
ALTER TABLE `cloud`.`pod_vlan_map` DROP INDEX `id` ;
ALTER TABLE `cloud`.`portable_ip_address` DROP INDEX `id` ;
ALTER TABLE `cloud`.`portable_ip_range` DROP INDEX `id` ;
ALTER TABLE `cloud`.`region` DROP INDEX `id` ;
ALTER TABLE `cloud`.`remote_access_vpn` DROP INDEX `id` ;
ALTER TABLE `cloud`.`snapshot_details` DROP INDEX `id` ;
ALTER TABLE `cloud`.`snapshots` DROP INDEX `id` ;
ALTER TABLE `cloud`.`storage_pool` DROP INDEX `id` ;
ALTER TABLE `cloud`.`storage_pool_details` DROP INDEX `id` ;
ALTER TABLE `cloud`.`storage_pool_work` DROP INDEX `id` ;
ALTER TABLE `cloud`.`user_ip_address` DROP INDEX `id` ;
ALTER TABLE `cloud`.`user_ipv6_address` DROP INDEX `id` ;
ALTER TABLE `cloud`.`user_statistics` DROP INDEX `id` ;
ALTER TABLE `cloud`.`version` DROP INDEX `id` ;
ALTER TABLE `cloud`.`vlan` DROP INDEX `id` ;
ALTER TABLE `cloud`.`vm_disk_statistics` DROP INDEX `id` ;
ALTER TABLE `cloud`.`vm_snapshot_details` DROP INDEX `id` ;
ALTER TABLE `cloud`.`vm_work_job` DROP INDEX `id` ;
ALTER TABLE `cloud`.`vpc_gateways` DROP INDEX `id` ;
ALTER TABLE `cloud`.`vpn_users` DROP INDEX `id` ;

