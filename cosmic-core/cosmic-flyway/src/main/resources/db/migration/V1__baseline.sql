CREATE TABLE `account` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `account_name` VARCHAR(100) NULL
  COMMENT 'an account name set by the creator of the account, defaults to username for single accounts',
  `uuid` VARCHAR(40) NULL,
  `type` INT(1) UNSIGNED NOT NULL,
  `domain_id` BIGINT NULL,
  `state` VARCHAR(10) DEFAULT 'enabled' NOT NULL,
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `cleanup_needed` TINYINT(1) DEFAULT '0' NOT NULL,
  `network_domain` VARCHAR(255) NULL,
  `default_zone_id` BIGINT NULL,
  `default` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if account is default',
  CONSTRAINT `uc_account__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_account__default_zone_id`
  ON `account` (`default_zone_id`);

CREATE INDEX `i_account__account_name__domain_id__removed`
  ON `account` (`account_name`, `domain_id`, `removed`);

CREATE INDEX `i_account__cleanup_needed`
  ON `account` (`cleanup_needed`);

CREATE INDEX `i_account__domain_id`
  ON `account` (`domain_id`);

CREATE INDEX `i_account__removed`
  ON `account` (`removed`);

CREATE TABLE `account_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `account_id` BIGINT NOT NULL
  COMMENT 'account id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NULL,
  CONSTRAINT `fk_account_details__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_account_details__account_id`
  ON `account_details` (`account_id`);

CREATE TABLE `account_network_ref` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `account_id` BIGINT NOT NULL
  COMMENT 'account id',
  `network_id` BIGINT NOT NULL
  COMMENT 'network id',
  `is_owner` SMALLINT(1) NOT NULL
  COMMENT 'is the owner of the network',
  CONSTRAINT `fk_account_network_ref__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_account_network_ref__account_id`
  ON `account_network_ref` (`account_id`);

CREATE INDEX `i_account_network_ref__networks_id`
  ON `account_network_ref` (`network_id`);

CREATE TABLE `account_vlan_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `account_id` BIGINT NOT NULL
  COMMENT 'account id. foreign key to account table',
  `vlan_db_id` BIGINT NOT NULL
  COMMENT 'database id of vlan. foreign key to vlan table',
  CONSTRAINT `fk_account_vlan_map__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_account_vlan_map__account_id`
  ON `account_vlan_map` (`account_id`);

CREATE INDEX `i_account_vlan_map__vlan_id`
  ON `account_vlan_map` (`vlan_db_id`);

CREATE TABLE `account_vnet_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(255) NULL,
  `vnet_range` VARCHAR(255) NOT NULL
  COMMENT 'dedicated guest vlan range',
  `account_id` BIGINT NOT NULL
  COMMENT 'account id. foreign key to account table',
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'physical network id. foreign key to the the physical network table',
  CONSTRAINT `uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_account_vnet_map__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_account_vnet_map__account_id`
  ON `account_vnet_map` (`account_id`);

CREATE INDEX `i_account_vnet_map__physical_network_id`
  ON `account_vnet_map` (`physical_network_id`);

CREATE TABLE `affinity_group` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `type` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL,
  `description` VARCHAR(4096) NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `acl_type` VARCHAR(15) NOT NULL
  COMMENT 'ACL access type. can be Account/Domain',
  CONSTRAINT `name`
  UNIQUE (`name`, `account_id`),
  CONSTRAINT `uc_affinity_group__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_affinity_group__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
);

CREATE INDEX `i_affinity_group__account_id`
  ON `affinity_group` (`account_id`);

CREATE INDEX `i_affinity_group__domain_id`
  ON `affinity_group` (`domain_id`);

CREATE TABLE `affinity_group_domain_map` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `affinity_group_id` BIGINT NOT NULL
  COMMENT 'affinity group id',
  `subdomain_access` INT(1) UNSIGNED DEFAULT '1' NULL
  COMMENT '1 if affinity group can be accessible from the subdomain',
  CONSTRAINT `fk_affinity_group_domain_map__affinity_group_id`
  FOREIGN KEY (`affinity_group_id`) REFERENCES `cloud`.`affinity_group` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_affinity_group_domain_map__affinity_group_id`
  ON `affinity_group_domain_map` (`affinity_group_id`);

CREATE INDEX `i_affinity_group_domain_map__domain_id`
  ON `affinity_group_domain_map` (`domain_id`);

CREATE TABLE `affinity_group_vm_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `affinity_group_id` BIGINT NOT NULL,
  `instance_id` BIGINT NOT NULL,
  CONSTRAINT `fk_agvm__group_id`
  FOREIGN KEY (`affinity_group_id`) REFERENCES `cloud`.`affinity_group` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_affinity_group_vm_map___instance_id`
  ON `affinity_group_vm_map` (`instance_id`);

CREATE INDEX `i_agvm__group_id`
  ON `affinity_group_vm_map` (`affinity_group_id`);

CREATE TABLE `alert` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `type` INT(1) UNSIGNED NOT NULL,
  `cluster_id` BIGINT NULL,
  `pod_id` BIGINT NULL,
  `data_center_id` BIGINT NOT NULL,
  `subject` VARCHAR(999) NULL
  COMMENT 'according to SMTP spec, max subject length is 1000 including the CRLF character, so allow enough space to fit long pod/zone/host names',
  `sent_count` INT(3) UNSIGNED NOT NULL,
  `created` DATETIME NULL
  COMMENT 'when this alert type was created',
  `last_sent` DATETIME NULL
  COMMENT 'Last time the alert was sent',
  `resolved` DATETIME NULL
  COMMENT 'when the alert status was resolved (available memory no longer at critical level, etc.)',
  `archived` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL,
  `name` VARCHAR(255) NULL
  COMMENT 'name of the alert',
  CONSTRAINT `uc_alert__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_alert__last_sent`
  ON `alert` (`last_sent`);

CREATE INDEX `type`
  ON `alert` (`type`);

CREATE TABLE `async_job` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `instance_type` VARCHAR(64) NULL
  COMMENT 'instance_type and instance_id work together to allow attaching an instance object to a job',
  `instance_id` BIGINT NULL,
  `job_cmd` VARCHAR(255) NULL,
  `job_cmd_info` TEXT NULL
  COMMENT 'command parameter info',
  `job_cmd_ver` INT(1) NULL
  COMMENT 'command version',
  `job_status` INT(1) NULL
  COMMENT 'general job execution status',
  `job_process_status` INT(1) NULL
  COMMENT 'job specific process status for asynchronize progress update',
  `job_result_code` INT(1) NULL
  COMMENT 'job result code, specify error code corresponding to result message',
  `job_result` TEXT NULL
  COMMENT 'job result info',
  `job_init_msid` BIGINT NULL
  COMMENT 'the initiating msid',
  `job_complete_msid` BIGINT NULL
  COMMENT 'completing msid',
  `created` DATETIME NULL
  COMMENT 'date created',
  `last_updated` DATETIME NULL
  COMMENT 'date created',
  `last_polled` DATETIME NULL
  COMMENT 'date polled',
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `uuid` VARCHAR(40) NULL,
  `related` CHAR(40) NOT NULL,
  `job_type` VARCHAR(32) NULL,
  `job_dispatcher` VARCHAR(64) NULL,
  `job_executing_msid` BIGINT NULL,
  `job_pending_signals` INT(10) DEFAULT '0' NOT NULL,
  CONSTRAINT `uc_async__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_async_job__removed`
  ON `async_job` (`removed`);

CREATE INDEX `i_async__account_id`
  ON `async_job` (`account_id`);

CREATE INDEX `i_async__created`
  ON `async_job` (`created`);

CREATE INDEX `i_async__instance_type_id`
  ON `async_job` (`instance_type`, `instance_id`);

CREATE INDEX `i_async__job_cmd`
  ON `async_job` (`job_cmd`);

CREATE INDEX `i_async__last_poll`
  ON `async_job` (`last_polled`);

CREATE INDEX `i_async__last_updated`
  ON `async_job` (`last_updated`);

CREATE INDEX `i_async__user_id`
  ON `async_job` (`user_id`);

CREATE TABLE `async_job_join_map` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `job_id` BIGINT NOT NULL,
  `join_job_id` BIGINT NOT NULL,
  `join_status` INT NOT NULL,
  `join_result` VARCHAR(1024) NULL,
  `join_msid` BIGINT NULL,
  `complete_msid` BIGINT NULL,
  `sync_source_id` BIGINT NULL
  COMMENT 'upper-level job sync source info before join',
  `wakeup_handler` VARCHAR(64) NULL,
  `wakeup_dispatcher` VARCHAR(64) NULL,
  `wakeup_interval` BIGINT DEFAULT '3000' NOT NULL
  COMMENT 'wakeup interval in seconds',
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `next_wakeup` DATETIME NULL,
  `expiration` DATETIME NULL,
  CONSTRAINT `fk_async_job_join_map__join`
  UNIQUE (`job_id`, `join_job_id`),
  CONSTRAINT `fk_async_job_join_map__job_id`
  FOREIGN KEY (`job_id`) REFERENCES `cloud`.`async_job` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_async_job_join_map__created`
  ON `async_job_join_map` (`created`);

CREATE INDEX `i_async_job_join_map__expiration`
  ON `async_job_join_map` (`expiration`);

CREATE INDEX `i_async_job_join_map__join_job_id`
  ON `async_job_join_map` (`join_job_id`);

CREATE INDEX `i_async_job_join_map__last_updated`
  ON `async_job_join_map` (`last_updated`);

CREATE INDEX `i_async_job_join_map__next_wakeup`
  ON `async_job_join_map` (`next_wakeup`);

CREATE TABLE `autoscale_policies` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `duration` INT(10) UNSIGNED NOT NULL,
  `quiet_time` INT(10) UNSIGNED NOT NULL,
  `last_quiet_time` DATETIME NULL,
  `action` VARCHAR(15) NULL,
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  CONSTRAINT `uc_autoscale_policies__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_autoscale_policies__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_autoscale_policies__account_id`
  ON `autoscale_policies` (`account_id`);

CREATE INDEX `i_autoscale_policies__domain_id`
  ON `autoscale_policies` (`domain_id`);

CREATE INDEX `i_autoscale_policies__removed`
  ON `autoscale_policies` (`removed`);

CREATE TABLE `autoscale_policy_condition_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `policy_id` BIGINT NOT NULL,
  `condition_id` BIGINT NOT NULL,
  CONSTRAINT `fk_autoscale_policy_condition_map__policy_id`
  FOREIGN KEY (`policy_id`) REFERENCES `cloud`.`autoscale_policies` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_autoscale_policy_condition_map__condition_id`
  ON `autoscale_policy_condition_map` (`condition_id`);

CREATE INDEX `i_autoscale_policy_condition_map__policy_id`
  ON `autoscale_policy_condition_map` (`policy_id`);

CREATE TABLE `autoscale_vmgroup_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `autoscale_vmgroup_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_autoscale_vmgroup_details__autoscale_vmgroup_id`
  ON `autoscale_vmgroup_details` (`autoscale_vmgroup_id`);

CREATE TABLE `autoscale_vmgroup_policy_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vmgroup_id` BIGINT NOT NULL,
  `policy_id` BIGINT NOT NULL,
  CONSTRAINT `fk_autoscale_vmgroup_policy_map__policy_id`
  FOREIGN KEY (`policy_id`) REFERENCES `cloud`.`autoscale_policies` (`id`)
);

CREATE INDEX `i_autoscale_vmgroup_policy_map__policy_id`
  ON `autoscale_vmgroup_policy_map` (`policy_id`);

CREATE INDEX `i_autoscale_vmgroup_policy_map__vmgroup_id`
  ON `autoscale_vmgroup_policy_map` (`vmgroup_id`);

CREATE TABLE `autoscale_vmgroup_vm_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vmgroup_id` BIGINT NOT NULL,
  `instance_id` BIGINT NOT NULL
);

CREATE INDEX `i_autoscale_vmgroup_vm_map__instance_id`
  ON `autoscale_vmgroup_vm_map` (`instance_id`);

CREATE INDEX `i_autoscale_vmgroup_vm_map__vmgroup_id`
  ON `autoscale_vmgroup_vm_map` (`vmgroup_id`);

CREATE TABLE `autoscale_vmgroups` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `zone_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `load_balancer_id` BIGINT NOT NULL,
  `min_members` INT(10) UNSIGNED DEFAULT '1' NULL,
  `max_members` INT(10) UNSIGNED NOT NULL,
  `member_port` INT(10) UNSIGNED NOT NULL,
  `interval` INT(10) UNSIGNED NOT NULL,
  `profile_id` BIGINT NOT NULL,
  `state` VARCHAR(255) NOT NULL
  COMMENT 'enabled or disabled, a vmgroup is disabled to stop autoscaling activity',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the entry can be displayed to the end user',
  `last_interval` DATETIME NULL
  COMMENT 'last updated time',
  CONSTRAINT `uc_autoscale_vmgroups__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_autoscale_vmgroups__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_autoscale_vmgroups__account_id`
  ON `autoscale_vmgroups` (`account_id`);

CREATE INDEX `i_autoscale_vmgroups__domain_id`
  ON `autoscale_vmgroups` (`domain_id`);

CREATE INDEX `i_autoscale_vmgroups__zone_id`
  ON `autoscale_vmgroups` (`zone_id`);

CREATE INDEX `i_autoscale_vmgroup__autoscale_vmprofile_id`
  ON `autoscale_vmgroups` (`profile_id`);

CREATE INDEX `i_autoscale_vmgroups__load_balancer_id`
  ON `autoscale_vmgroups` (`load_balancer_id`);

CREATE INDEX `i_autoscale_vmgroups__removed`
  ON `autoscale_vmgroups` (`removed`);

ALTER TABLE `autoscale_vmgroup_details`
  ADD CONSTRAINT `fk_autoscale_vmgroup_details__autoscale_vmgroup_id`
FOREIGN KEY (`autoscale_vmgroup_id`) REFERENCES `cloud`.`autoscale_vmgroups` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `autoscale_vmgroup_policy_map`
  ADD CONSTRAINT `fk_autoscale_vmgroup_policy_map__vmgroup_id`
FOREIGN KEY (`vmgroup_id`) REFERENCES `cloud`.`autoscale_vmgroups` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `autoscale_vmgroup_vm_map`
  ADD CONSTRAINT `fk_autoscale_vmgroup_vm_map__vmgroup_id`
FOREIGN KEY (`vmgroup_id`) REFERENCES `cloud`.`autoscale_vmgroups` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `autoscale_vmprofile_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `autoscale_vmprofile_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_autoscale_vmprofile_details__autoscale_vmprofile_id`
  ON `autoscale_vmprofile_details` (`autoscale_vmprofile_id`);

CREATE TABLE `autoscale_vmprofiles` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `zone_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `autoscale_user_id` BIGINT NOT NULL,
  `service_offering_id` BIGINT NOT NULL,
  `template_id` BIGINT NOT NULL,
  `other_deploy_params` VARCHAR(1024) NULL
  COMMENT 'other deployment parameters that is in addition to zoneid,serviceofferingid,domainid',
  `destroy_vm_grace_period` INT(10) UNSIGNED NULL
  COMMENT 'the time allowed for existing connections to get closed before a vm is destroyed',
  `counter_params` VARCHAR(1024) NULL
  COMMENT 'the parameters for the counter to be used to get metric information from VMs',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the entry can be displayed to the end user',
  CONSTRAINT `uc_autoscale_vmprofiles__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_autoscale_vmprofiles__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_autoscale_vmprofiles__account_id`
  ON `autoscale_vmprofiles` (`account_id`);

CREATE INDEX `i_autoscale_vmprofiles__autoscale_user_id`
  ON `autoscale_vmprofiles` (`autoscale_user_id`);

CREATE INDEX `i_autoscale_vmprofiles__domain_id`
  ON `autoscale_vmprofiles` (`domain_id`);

CREATE INDEX `i_autoscale_vmprofiles__removed`
  ON `autoscale_vmprofiles` (`removed`);

ALTER TABLE `autoscale_vmgroups`
  ADD CONSTRAINT `fk_autoscale_vmgroup__autoscale_vmprofile_id`
FOREIGN KEY (`profile_id`) REFERENCES `cloud`.`autoscale_vmprofiles` (`id`);

ALTER TABLE `autoscale_vmprofile_details`
  ADD CONSTRAINT `fk_autoscale_vmprofile_details__autoscale_vmprofile_id`
FOREIGN KEY (`autoscale_vmprofile_id`) REFERENCES `cloud`.`autoscale_vmprofiles` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `cluster` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `name` VARCHAR(255) NULL
  COMMENT 'name for the cluster',
  `uuid` VARCHAR(40) NULL
  COMMENT 'uuid is different with following guid, while the later one is generated by hypervisor resource',
  `guid` VARCHAR(255) NULL
  COMMENT 'guid for the cluster',
  `pod_id` BIGINT NOT NULL
  COMMENT 'pod id',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center id',
  `hypervisor_type` VARCHAR(32) NULL,
  `cluster_type` VARCHAR(64) DEFAULT 'CloudManaged' NULL,
  `allocation_state` VARCHAR(32) DEFAULT 'Enabled' NOT NULL
  COMMENT 'Is this cluster enabled for allocation for new resources',
  `managed_state` VARCHAR(32) DEFAULT 'Managed' NOT NULL
  COMMENT 'Is this cluster managed by cloudstack',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `owner` VARCHAR(255) NULL,
  `created` DATETIME NULL
  COMMENT 'date created',
  `lastUpdated` DATETIME NULL
  COMMENT 'last updated',
  `engine_state` VARCHAR(32) DEFAULT 'Disabled' NOT NULL
  COMMENT 'the engine state of the zone',
  CONSTRAINT `uc_cluster__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `guid`
  UNIQUE (`guid`),
  CONSTRAINT `i_cluster__pod_id__name`
  UNIQUE (`pod_id`, `name`)
);

CREATE INDEX `i_cluster__data_center_id`
  ON `cluster` (`data_center_id`);

CREATE INDEX `i_cluster__allocation_state`
  ON `cluster` (`allocation_state`);

CREATE INDEX `i_cluster__removed`
  ON `cluster` (`removed`);

CREATE TABLE `cluster_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `cluster_id` BIGINT NOT NULL
  COMMENT 'cluster id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NULL,
  CONSTRAINT `fk_cluster_details__cluster_id`
  FOREIGN KEY (`cluster_id`) REFERENCES `cloud`.`cluster` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_cluster_details__cluster_id`
  ON `cluster_details` (`cluster_id`);

CREATE TABLE `conditions` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `counter_id` BIGINT NOT NULL
  COMMENT 'Counter Id',
  `threshold` BIGINT NOT NULL
  COMMENT 'threshold value for the given counter',
  `relational_operator` CHAR(2) NULL
  COMMENT 'relational operator to be used upon the counter and condition',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain the Condition belongs to',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner of this Condition',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  CONSTRAINT `uc_conditions__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_conditions__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_conditions__account_id`
  ON `conditions` (`account_id`);

CREATE INDEX `i_conditions__counter_id`
  ON `conditions` (`counter_id`);

CREATE INDEX `i_conditions__domain_id`
  ON `conditions` (`domain_id`);

CREATE INDEX `i_conditions__removed`
  ON `conditions` (`removed`);

ALTER TABLE `autoscale_policy_condition_map`
  ADD CONSTRAINT `fk_autoscale_policy_condition_map__condition_id`
FOREIGN KEY (`condition_id`) REFERENCES `cloud`.`conditions` (`id`);

CREATE TABLE `configuration` (
  `category` VARCHAR(255) DEFAULT 'Advanced' NOT NULL,
  `instance` VARCHAR(255) NOT NULL,
  `component` VARCHAR(255) DEFAULT 'management-server' NOT NULL,
  `name` VARCHAR(255) NOT NULL
    PRIMARY KEY,
  `value` VARCHAR(8191) NULL,
  `description` VARCHAR(1024) NULL,
  `default_value` VARCHAR(8191) NULL,
  `updated` DATETIME NULL
  COMMENT 'Time this was updated by the server. null means this row is obsolete.',
  `scope` VARCHAR(255) NULL
  COMMENT 'Can this parameter be scoped',
  `is_dynamic` TINYINT(1) DEFAULT '0' NOT NULL
  COMMENT 'Can the parameter be change dynamically without restarting the server'
);

CREATE INDEX `i_configuration__category`
  ON `configuration` (`category`);

CREATE INDEX `i_configuration__component`
  ON `configuration` (`component`);

CREATE INDEX `i_configuration__instance`
  ON `configuration` (`instance`);

CREATE INDEX `i_configuration__name`
  ON `configuration` (`name`);

CREATE TABLE `console_proxy` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `public_mac_address` VARCHAR(17) NULL
  COMMENT 'mac address of the public facing network card',
  `public_ip_address` CHAR(40) NULL
  COMMENT 'public ip address for the console proxy',
  `public_netmask` VARCHAR(15) NULL
  COMMENT 'public netmask used for the console proxy',
  `active_session` INT(10) DEFAULT '0' NOT NULL
  COMMENT 'active session number',
  `last_update` DATETIME NULL
  COMMENT 'Last session update time',
  `session_details` BLOB NULL
  COMMENT 'session detail info',
  CONSTRAINT `public_mac_address`
  UNIQUE (`public_mac_address`)
);

CREATE TABLE `counter` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `source` VARCHAR(255) NOT NULL
  COMMENT 'source e.g. netscaler, snmp',
  `name` VARCHAR(255) NOT NULL
  COMMENT 'Counter name',
  `value` VARCHAR(255) NOT NULL
  COMMENT 'Value in case of source=snmp',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  CONSTRAINT `uc_counter__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_counter__removed`
  ON `counter` (`removed`);

CREATE INDEX `i_counter__source`
  ON `counter` (`source`);

ALTER TABLE `conditions`
  ADD CONSTRAINT `fk_conditions__counter_id`
FOREIGN KEY (`counter_id`) REFERENCES `cloud`.`counter` (`id`);

CREATE TABLE `data_center` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NULL,
  `uuid` VARCHAR(40) NULL,
  `description` VARCHAR(255) NULL,
  `dns1` VARCHAR(255) NOT NULL,
  `dns2` VARCHAR(255) NULL,
  `internal_dns1` VARCHAR(255) NOT NULL,
  `internal_dns2` VARCHAR(255) NULL,
  `gateway` VARCHAR(15) NULL,
  `netmask` VARCHAR(15) NULL,
  `router_mac_address` VARCHAR(17) DEFAULT '02:00:00:00:00:01' NOT NULL
  COMMENT 'mac address for the router within the domain',
  `mac_address` BIGINT DEFAULT '1' NOT NULL
  COMMENT 'Next available mac address for the ethernet card interacting with public internet',
  `guest_network_cidr` VARCHAR(18) NULL,
  `domain` VARCHAR(100) NULL
  COMMENT 'Network domain name of the Vms of the zone',
  `domain_id` BIGINT NULL
  COMMENT 'domain id for the parent domain to this zone (null signifies public zone)',
  `networktype` VARCHAR(255) DEFAULT 'Basic' NOT NULL
  COMMENT 'Network type of the zone',
  `dns_provider` CHAR(64) DEFAULT 'VirtualRouter' NULL,
  `gateway_provider` CHAR(64) DEFAULT 'VirtualRouter' NULL,
  `firewall_provider` CHAR(64) DEFAULT 'VirtualRouter' NULL,
  `dhcp_provider` CHAR(64) DEFAULT 'VirtualRouter' NULL,
  `lb_provider` CHAR(64) DEFAULT 'VirtualRouter' NULL,
  `vpn_provider` CHAR(64) DEFAULT 'VirtualRouter' NULL,
  `userdata_provider` CHAR(64) DEFAULT 'VirtualRouter' NULL,
  `allocation_state` VARCHAR(32) DEFAULT 'Enabled' NOT NULL
  COMMENT 'Is this data center enabled for allocation for new resources',
  `zone_token` VARCHAR(255) NULL,
  `is_security_group_enabled` TINYINT DEFAULT '0' NOT NULL
  COMMENT '1: enabled, 0: not',
  `is_local_storage_enabled` TINYINT DEFAULT '0' NOT NULL
  COMMENT 'Is local storage offering enabled for this data center; 1: enabled, 0: not',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `owner` VARCHAR(255) NULL,
  `created` DATETIME NULL
  COMMENT 'date created',
  `lastUpdated` DATETIME NULL
  COMMENT 'last updated',
  `engine_state` VARCHAR(32) DEFAULT 'Disabled' NOT NULL
  COMMENT 'the engine state of the zone',
  `ip6_dns1` VARCHAR(255) NULL,
  `ip6_dns2` VARCHAR(255) NULL,
  CONSTRAINT `uc_data_center__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_data_center__allocation_state`
  ON `data_center` (`allocation_state`);

CREATE INDEX `i_data_center__domain_id`
  ON `data_center` (`domain_id`);

CREATE INDEX `i_data_center__removed`
  ON `data_center` (`removed`);

CREATE INDEX `i_data_center__zone_token`
  ON `data_center` (`zone_token`);

ALTER TABLE `account`
  ADD CONSTRAINT `fk_account__default_zone_id`
FOREIGN KEY (`default_zone_id`) REFERENCES `cloud`.`data_center` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `autoscale_vmgroups`
  ADD CONSTRAINT `fk_autoscale_vmgroups__zone_id`
FOREIGN KEY (`zone_id`) REFERENCES `cloud`.`data_center` (`id`);

ALTER TABLE `cluster`
  ADD CONSTRAINT `fk_cluster__data_center_id`
FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `data_center_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `dc_id` BIGINT NOT NULL
  COMMENT 'dc id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_dc_details__dc_id`
  FOREIGN KEY (`dc_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_dc_details__dc_id`
  ON `data_center_details` (`dc_id`);

CREATE TABLE `dc_storage_network_ip_range` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `start_ip` CHAR(40) NOT NULL
  COMMENT 'start ip address',
  `end_ip` CHAR(40) NOT NULL
  COMMENT 'end ip address',
  `gateway` VARCHAR(15) NOT NULL
  COMMENT 'gateway ip address',
  `vlan` INT(10) UNSIGNED NULL
  COMMENT 'vlan the storage network on',
  `netmask` VARCHAR(15) NOT NULL
  COMMENT 'netmask for storage network',
  `data_center_id` BIGINT NOT NULL,
  `pod_id` BIGINT NOT NULL
  COMMENT 'pod it belongs to',
  `network_id` BIGINT NOT NULL
  COMMENT 'id of corresponding network offering',
  CONSTRAINT `uc_storage_ip_range__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_storage_ip_range__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
);

CREATE INDEX `i_storage_ip_range__data_center_id`
  ON `dc_storage_network_ip_range` (`data_center_id`);

CREATE INDEX `i_storage_ip_range__network_id`
  ON `dc_storage_network_ip_range` (`network_id`);

CREATE INDEX `i_storage_ip_range__pod_id`
  ON `dc_storage_network_ip_range` (`pod_id`);

CREATE TABLE `dedicated_resources` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `data_center_id` BIGINT NULL
  COMMENT 'data center id',
  `pod_id` BIGINT NULL
  COMMENT 'pod id',
  `cluster_id` BIGINT NULL
  COMMENT 'cluster id',
  `host_id` BIGINT NULL
  COMMENT 'host id',
  `domain_id` BIGINT NULL
  COMMENT 'domain id of the domain to which resource is dedicated',
  `account_id` BIGINT NULL
  COMMENT 'account id of the account to which resource is dedicated',
  `affinity_group_id` BIGINT NOT NULL
  COMMENT 'affinity group id associated',
  CONSTRAINT `uc_dedicated_resources__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_dedicated_resources__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_dedicated_resources__cluster_id`
  FOREIGN KEY (`cluster_id`) REFERENCES `cloud`.`cluster` (`id`),
  CONSTRAINT `fk_dedicated_resources__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`),
  CONSTRAINT `fk_dedicated_resources__affinity_group_id`
  FOREIGN KEY (`affinity_group_id`) REFERENCES `cloud`.`affinity_group` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_dedicated_resources__cluster_id`
  ON `dedicated_resources` (`cluster_id`);

CREATE INDEX `i_dedicated_resources__data_center_id`
  ON `dedicated_resources` (`data_center_id`);

CREATE INDEX `i_dedicated_resources__host_id`
  ON `dedicated_resources` (`host_id`);

CREATE INDEX `i_dedicated_resources__pod_id`
  ON `dedicated_resources` (`pod_id`);

CREATE INDEX `i_dedicated_resources_account_id`
  ON `dedicated_resources` (`account_id`);

CREATE INDEX `i_dedicated_resources_affinity_group_id`
  ON `dedicated_resources` (`affinity_group_id`);

CREATE INDEX `i_dedicated_resources_domain_id`
  ON `dedicated_resources` (`domain_id`);

CREATE TABLE `disk_offering` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `domain_id` BIGINT NULL,
  `name` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL,
  `display_text` VARCHAR(4096) NULL
  COMMENT 'Descrianaption text set by the admin for display purpose only',
  `disk_size` BIGINT NOT NULL
  COMMENT 'disk space in byte',
  `type` VARCHAR(32) NULL
  COMMENT 'inheritted by who?',
  `tags` VARCHAR(4096) NULL
  COMMENT 'comma separated tags about the disk_offering',
  `recreatable` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'The root disk is always recreatable',
  `use_local_storage` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Indicates whether local storage pools should be used',
  `unique_name` VARCHAR(32) NULL
  COMMENT 'unique name',
  `system_use` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'is this offering for system used only',
  `customized` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '0 implies not customized by default',
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `created` DATETIME NULL
  COMMENT 'date the disk offering was created',
  `sort_key` INT(32) DEFAULT '0' NOT NULL
  COMMENT 'sort key used for customising sort method',
  `display_offering` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'Should disk offering be displayed to the end user',
  `customized_iops` TINYINT(1) UNSIGNED NULL
  COMMENT 'Should customized IOPS be displayed to the end user',
  `min_iops` BIGINT NULL
  COMMENT 'Minimum IOPS',
  `max_iops` BIGINT NULL
  COMMENT 'Maximum IOPS',
  `bytes_read_rate` BIGINT NULL,
  `bytes_write_rate` BIGINT NULL,
  `iops_read_rate` BIGINT NULL,
  `iops_write_rate` BIGINT NULL,
  `state` CHAR(40) DEFAULT 'Active' NOT NULL
  COMMENT 'state for disk offering',
  `hv_ss_reserve` INT(32) UNSIGNED NULL
  COMMENT 'Hypervisor snapshot reserve space as a percent of a volume (for managed storage using Xen or VMware)',
  `cache_mode` VARCHAR(16) DEFAULT 'none' NULL
  COMMENT 'The disk cache mode to use for disks created with this offering',
  `provisioning_type` VARCHAR(32) DEFAULT 'thin' NOT NULL
  COMMENT 'pre allocation setting of the volume',
  CONSTRAINT `uc_disk_offering__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `unique_name`
  UNIQUE (`unique_name`)
);

CREATE INDEX `i_disk_offering__removed`
  ON `disk_offering` (`removed`);

CREATE TABLE `disk_offering_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `offering_id` BIGINT NOT NULL
  COMMENT 'offering id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_offering_details__offering_id`
  FOREIGN KEY (`offering_id`) REFERENCES `cloud`.`disk_offering` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_offering_details__offering_id`
  ON `disk_offering_details` (`offering_id`);

CREATE TABLE `domain` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `parent` BIGINT NULL,
  `name` VARCHAR(255) NULL,
  `uuid` VARCHAR(40) NULL,
  `owner` BIGINT NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `level` INT(10) DEFAULT '0' NOT NULL,
  `child_count` INT(10) DEFAULT '0' NOT NULL,
  `next_child_seq` BIGINT DEFAULT '1' NOT NULL,
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `state` CHAR(32) DEFAULT 'Active' NOT NULL
  COMMENT 'state of the domain',
  `network_domain` VARCHAR(255) NULL,
  `type` VARCHAR(255) DEFAULT 'Normal' NOT NULL
  COMMENT 'type of the domain - can be Normal or Project',
  `email` varchar(255) NULL,
  CONSTRAINT `parent`
  UNIQUE (`parent`, `name`, `removed`),
  CONSTRAINT `uc_domain__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_domain__path`
  ON `domain` (`path`);

CREATE INDEX `i_domain__removed`
  ON `domain` (`removed`);

ALTER TABLE `account`
  ADD CONSTRAINT `fk_account__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`);

ALTER TABLE `affinity_group`
  ADD CONSTRAINT `fk_affinity_group__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`);

ALTER TABLE `affinity_group_domain_map`
  ADD CONSTRAINT `fk_affinity_group_domain_map__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `autoscale_policies`
  ADD CONSTRAINT `fk_autoscale_policies__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `autoscale_vmgroups`
  ADD CONSTRAINT `fk_autoscale_vmgroups__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `autoscale_vmprofiles`
  ADD CONSTRAINT `fk_autoscale_vmprofiles__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `conditions`
  ADD CONSTRAINT `fk_conditions__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `data_center`
  ADD CONSTRAINT `fk_data_center__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`);

ALTER TABLE `dedicated_resources`
  ADD CONSTRAINT `fk_dedicated_resources__domain_id`
FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`);

CREATE TABLE `domain_network_ref` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `network_id` BIGINT NOT NULL
  COMMENT 'network id',
  `subdomain_access` INT(1) UNSIGNED NULL
  COMMENT '1 if network can be accessible from the subdomain',
  CONSTRAINT `fk_domain_network_ref__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_domain_network_ref__domain_id`
  ON `domain_network_ref` (`domain_id`);

CREATE INDEX `i_domain_network_ref__networks_id`
  ON `domain_network_ref` (`network_id`);

CREATE TABLE `domain_router` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `element_id` BIGINT NOT NULL
  COMMENT 'correlated virtual router provider ID',
  `public_mac_address` VARCHAR(17) NULL
  COMMENT 'mac address of the public facing network card',
  `public_ip_address` CHAR(40) NULL
  COMMENT 'public ip address used for source net',
  `public_netmask` VARCHAR(15) NULL
  COMMENT 'netmask used for the domR',
  `guest_netmask` VARCHAR(15) NULL
  COMMENT 'netmask used for the guest network',
  `guest_ip_address` CHAR(40) NULL
  COMMENT ' ip address in the guest network',
  `is_redundant_router` INT(1) UNSIGNED NOT NULL
  COMMENT 'if in redundant router mode',
  `priority` INT(4) UNSIGNED NULL
  COMMENT 'priority of router in the redundant router mode',
  `redundant_state` VARCHAR(64) NOT NULL
  COMMENT 'the state of redundant virtual router',
  `stop_pending` INT(1) UNSIGNED NOT NULL
  COMMENT 'if this router would be stopped after we can connect to it',
  `role` VARCHAR(64) NOT NULL
  COMMENT 'type of role played by this router',
  `template_version` VARCHAR(100) NULL
  COMMENT 'template version',
  `scripts_version` VARCHAR(100) NULL
  COMMENT 'scripts version',
  `vpc_id` BIGINT NULL
  COMMENT 'correlated virtual router vpc ID'
)
  COMMENT 'information about the domR instance';

CREATE INDEX `i_domain_router__element_id`
  ON `domain_router` (`element_id`);

CREATE INDEX `i_domain_router__vpc_id`
  ON `domain_router` (`vpc_id`);

CREATE TABLE `domain_vlan_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id. foreign key to domain table',
  `vlan_db_id` BIGINT NOT NULL
  COMMENT 'database id of vlan. foreign key to vlan table',
  CONSTRAINT `id`
  UNIQUE (`id`),
  CONSTRAINT `fk_domain_vlan_map__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_account_vlan_map__domain_id`
  ON `domain_vlan_map` (`domain_id`);

CREATE INDEX `i_account_vlan_map__vlan_id`
  ON `domain_vlan_map` (`vlan_db_id`);

CREATE TABLE `event` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `type` VARCHAR(32) NOT NULL,
  `state` VARCHAR(32) DEFAULT 'Completed' NOT NULL,
  `description` VARCHAR(1024) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `level` VARCHAR(16) NOT NULL,
  `start_id` BIGINT DEFAULT '0' NOT NULL,
  `parameters` VARCHAR(1024) NULL,
  `archived` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `uc_event__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `archived`
  ON `event` (`archived`);

CREATE INDEX `i_event__account_id`
  ON `event` (`account_id`);

CREATE INDEX `i_event__created`
  ON `event` (`created`);

CREATE INDEX `i_event__level_id`
  ON `event` (`level`);

CREATE INDEX `i_event__type_id`
  ON `event` (`type`);

CREATE INDEX `i_event__user_id`
  ON `event` (`user_id`);

CREATE INDEX `state`
  ON `event` (`state`);

CREATE TABLE `external_load_balancer_devices` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(255) NULL,
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'id of the physical network in to which load balancer device is added',
  `provider_name` VARCHAR(255) NOT NULL
  COMMENT 'Service Provider name corresponding to this load balancer device',
  `device_name` VARCHAR(255) NOT NULL
  COMMENT 'name of the load balancer device',
  `capacity` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'Capacity of the load balancer device',
  `device_state` VARCHAR(32) DEFAULT 'Disabled' NOT NULL
  COMMENT 'state (enabled/disabled/shutdown) of the device',
  `allocation_state` VARCHAR(32) DEFAULT 'Free' NOT NULL
  COMMENT 'Allocation state (Free/Shared/Dedicated/Provider) of the device',
  `is_dedicated` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if device/appliance is provisioned for dedicated use only',
  `is_managed` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if load balancer appliance is provisioned and its life cycle is managed by by cloudstack',
  `host_id` BIGINT NOT NULL
  COMMENT 'host id coresponding to the external load balancer device',
  `parent_host_id` BIGINT NULL
  COMMENT 'if the load balancer appliance is cloudstack managed, then host id on which this appliance is provisioned',
  `is_gslb_provider` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if load balancer appliance is acting as gslb service provider in the zone',
  `gslb_site_publicip` VARCHAR(255) NULL
  COMMENT 'GSLB service Provider site public ip',
  `gslb_site_privateip` VARCHAR(255) NULL
  COMMENT 'GSLB service Provider site private ip',
  `is_exclusive_gslb_provider` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if load balancer appliance is acting exclusively as gslb service provider in the zone and can not be used for LB',
  CONSTRAINT `uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_external_lb_devices_parent_host_id`
  ON `external_load_balancer_devices` (`host_id`);

CREATE INDEX `i_external_lb_devices_physical_network_id`
  ON `external_load_balancer_devices` (`physical_network_id`);

CREATE TABLE `external_nicira_nvp_devices` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(255) NULL,
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'id of the physical network in to which nicira nvp device is added',
  `provider_name` VARCHAR(255) NOT NULL
  COMMENT 'Service Provider name corresponding to this nicira nvp device',
  `device_name` VARCHAR(255) NOT NULL
  COMMENT 'name of the nicira nvp device',
  `host_id` BIGINT NOT NULL
  COMMENT 'host id coresponding to the external nicira nvp device',
  CONSTRAINT `uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_external_nicira_nvp_devices__host_id`
  ON `external_nicira_nvp_devices` (`host_id`);

CREATE INDEX `i_external_nicira_nvp_devices__physical_network_id`
  ON `external_nicira_nvp_devices` (`physical_network_id`);

CREATE TABLE `external_nuage_vsp_devices` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(255) NULL,
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'id of the physical network in to which nuage vsp is added',
  `provider_name` VARCHAR(255) NOT NULL
  COMMENT 'the service provider name corresponding to this nuage vsp device',
  `device_name` VARCHAR(255) NOT NULL
  COMMENT 'the name of the nuage vsp device',
  `host_id` BIGINT NOT NULL
  COMMENT 'host id corresponding to the external nuage vsp device',
  CONSTRAINT `uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_external_nuage_vsp_devices__host_id`
  ON `external_nuage_vsp_devices` (`host_id`);

CREATE INDEX `i_external_nuage_vsp_devices__physical_network_id`
  ON `external_nuage_vsp_devices` (`physical_network_id`);

CREATE TABLE `external_stratosphere_ssp_credentials` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `data_center_id` BIGINT NOT NULL,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL
);

CREATE TABLE `external_stratosphere_ssp_tenants` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(255) NOT NULL
  COMMENT 'SSP tenant uuid',
  `zone_id` BIGINT NOT NULL
  COMMENT 'cloudstack zone_id'
);

CREATE TABLE `external_stratosphere_ssp_uuids` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(255) NOT NULL
  COMMENT 'uuid provided by SSP',
  `obj_class` VARCHAR(255) NOT NULL,
  `obj_id` BIGINT NOT NULL,
  `reservation_id` VARCHAR(255) NULL
);

CREATE TABLE `firewall_rule_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `firewall_rule_id` BIGINT NOT NULL
  COMMENT 'Firewall rule id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_firewall_rule_details__firewall_rule_id`
  ON `firewall_rule_details` (`firewall_rule_id`);

CREATE TABLE `firewall_rules` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `ip_address_id` BIGINT NULL
  COMMENT 'id of the corresponding ip address',
  `start_port` INT(10) NULL
  COMMENT 'starting port of a port range',
  `end_port` INT(10) NULL
  COMMENT 'end port of a port range',
  `state` CHAR(32) NOT NULL
  COMMENT 'current state of this rule',
  `protocol` CHAR(16) DEFAULT 'TCP' NOT NULL
  COMMENT 'protocol to open these ports for',
  `purpose` CHAR(32) NOT NULL
  COMMENT 'why are these ports opened?',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner id',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `network_id` BIGINT NOT NULL
  COMMENT 'network id',
  `xid` CHAR(40) NOT NULL
  COMMENT 'external id',
  `created` DATETIME NULL
  COMMENT 'Date created',
  `icmp_code` INT(10) NULL
  COMMENT 'The ICMP code (if protocol=ICMP). A value of -1 means all codes for the given ICMP type.',
  `icmp_type` INT(10) NULL
  COMMENT 'The ICMP type (if protocol=ICMP). A value of -1 means all types.',
  `related` BIGINT NULL
  COMMENT 'related to what other firewall rule',
  `type` VARCHAR(10) DEFAULT 'USER' NOT NULL,
  `vpc_id` BIGINT NULL
  COMMENT 'vpc the firewall rule is associated with',
  `traffic_type` CHAR(32) NULL
  COMMENT 'the traffic type of the rule, can be Ingress or Egress',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the rule can be displayed to the end user',
  CONSTRAINT `uc_firewall_rules__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_firewall_rules__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__related`
  FOREIGN KEY (`related`) REFERENCES `cloud`.`firewall_rules` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_firewall_rules__account_id`
  ON `firewall_rules` (`account_id`);

CREATE INDEX `i_firewall_rules__domain_id`
  ON `firewall_rules` (`domain_id`);

CREATE INDEX `i_firewall_rules__ip_address_id`
  ON `firewall_rules` (`ip_address_id`);

CREATE INDEX `i_firewall_rules__network_id`
  ON `firewall_rules` (`network_id`);

CREATE INDEX `i_firewall_rules__related`
  ON `firewall_rules` (`related`);

CREATE INDEX `i_firewall_rules__vpc_id`
  ON `firewall_rules` (`vpc_id`);

CREATE INDEX `i_firewall_rules__purpose`
  ON `firewall_rules` (`purpose`);

ALTER TABLE `firewall_rule_details`
  ADD CONSTRAINT `fk_firewall_rule_details__firewall_rule_id`
FOREIGN KEY (`firewall_rule_id`) REFERENCES `cloud`.`firewall_rules` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `firewall_rules_cidrs` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `firewall_rule_id` BIGINT NOT NULL
  COMMENT 'firewall rule id',
  `source_cidr` VARCHAR(18) NULL,
  CONSTRAINT `unique_rule_cidrs`
  UNIQUE (`firewall_rule_id`, `source_cidr`),
  CONSTRAINT `fk_firewall_cidrs_firewall_rules`
  FOREIGN KEY (`firewall_rule_id`) REFERENCES `cloud`.`firewall_rules` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_firewall_cidrs_firewall_rules`
  ON `firewall_rules_cidrs` (`firewall_rule_id`);

CREATE TABLE `global_load_balancer_lb_rule_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `gslb_rule_id` BIGINT NOT NULL,
  `lb_rule_id` BIGINT NOT NULL,
  `weight` BIGINT DEFAULT '1' NOT NULL
  COMMENT 'weight of the site in gslb',
  `revoke` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 is when rule is set for Revoke',
  CONSTRAINT `gslb_rule_id`
  UNIQUE (`gslb_rule_id`, `lb_rule_id`)
);

CREATE INDEX `i_lb_rule_id`
  ON `global_load_balancer_lb_rule_map` (`lb_rule_id`);

CREATE TABLE `global_load_balancing_rules` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `account_id` BIGINT NOT NULL
  COMMENT 'account id',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `region_id` INT(10) UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(4096) NULL
  COMMENT 'description',
  `state` CHAR(32) NOT NULL
  COMMENT 'current state of this rule',
  `algorithm` VARCHAR(255) NOT NULL
  COMMENT 'load balancing algorithm used to distribbute traffic across zones',
  `persistence` VARCHAR(255) NOT NULL
  COMMENT 'session persistence used across the zone',
  `service_type` VARCHAR(255) NOT NULL
  COMMENT 'GSLB service type (tcp/udp)',
  `gslb_domain_name` VARCHAR(255) NOT NULL
  COMMENT 'DNS name for the GSLB service that is used to provide a FQDN for the GSLB service',
  CONSTRAINT `fk_global_load_balancing_rules_account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_global_load_balancing_rules_account_id`
  ON `global_load_balancing_rules` (`account_id`);

CREATE INDEX `i_global_load_balancing_rules_region_id`
  ON `global_load_balancing_rules` (`region_id`);

ALTER TABLE `global_load_balancer_lb_rule_map`
  ADD CONSTRAINT `fk_gslb_rule_id`
FOREIGN KEY (`gslb_rule_id`) REFERENCES `cloud`.`global_load_balancing_rules` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `guest_os` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `category_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NULL,
  `uuid` VARCHAR(40) NULL,
  `display_name` VARCHAR(255) NOT NULL,
  `created` DATETIME NULL
  COMMENT 'Time when Guest OS was created in system',
  `removed` DATETIME NULL
  COMMENT 'Time when Guest OS was removed if deleted, else NULL',
  `is_user_defined` INT(1) UNSIGNED DEFAULT '0' NULL
  COMMENT 'True if this guest OS type was added by admin',
  `manufacturer_string` VARCHAR(64) DEFAULT 'Mission Critical Cloud' NULL
  COMMENT 'String to put in the Manufacturer field in the XML of a KVM VM',
  CONSTRAINT `uc_guest_os__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_guest_os__category_id`
  ON `guest_os` (`category_id`);

CREATE TABLE `guest_os_category` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL,
  CONSTRAINT `uc_guest_os_category__uuid`
  UNIQUE (`uuid`)
);

ALTER TABLE `guest_os`
  ADD CONSTRAINT `fk_guest_os__category_id`
FOREIGN KEY (`category_id`) REFERENCES `cloud`.`guest_os_category` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `guest_os_hypervisor` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `hypervisor_type` VARCHAR(32) NOT NULL,
  `guest_os_name` VARCHAR(255) NOT NULL,
  `guest_os_id` BIGINT NOT NULL,
  `hypervisor_version` VARCHAR(32) DEFAULT 'default' NOT NULL
  COMMENT 'Hypervisor version for this mapping',
  `uuid` VARCHAR(40) NULL
  COMMENT 'UUID of the mapping',
  `created` DATETIME NULL
  COMMENT 'Time when mapping was created',
  `removed` DATETIME NULL
  COMMENT 'Time when mapping was removed if deleted, else NULL',
  `is_user_defined` INT(1) UNSIGNED DEFAULT '0' NULL
  COMMENT 'True if this guest OS mapping was added by admin',
  CONSTRAINT `uc_guest_os_hypervisor__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `guest_os_hypervisor_ibfk_1`
  FOREIGN KEY (`guest_os_id`) REFERENCES `cloud`.`guest_os` (`id`)
);

CREATE INDEX `guest_os_id`
  ON `guest_os_hypervisor` (`guest_os_id`);

CREATE TABLE `host` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL
  COMMENT 'this uuid is different with guid below, the later one is used by hypervisor resource',
  `status` VARCHAR(32) NOT NULL,
  `type` VARCHAR(32) NOT NULL,
  `private_ip_address` CHAR(40) NOT NULL,
  `private_netmask` VARCHAR(15) NULL,
  `private_mac_address` VARCHAR(17) NULL,
  `storage_ip_address` CHAR(40) NULL,
  `storage_netmask` VARCHAR(15) NULL,
  `storage_mac_address` VARCHAR(17) NULL,
  `storage_ip_address_2` CHAR(40) NULL,
  `storage_mac_address_2` VARCHAR(17) NULL,
  `storage_netmask_2` VARCHAR(15) NULL,
  `cluster_id` BIGINT NULL
  COMMENT 'foreign key to cluster',
  `public_ip_address` CHAR(40) NULL,
  `public_netmask` VARCHAR(15) NULL,
  `public_mac_address` VARCHAR(17) NULL,
  `proxy_port` INT(10) UNSIGNED NULL,
  `data_center_id` BIGINT NOT NULL,
  `pod_id` BIGINT NULL,
  `cpu_sockets` INT(10) UNSIGNED NULL
  COMMENT 'the number of CPU sockets on the host',
  `cpus` INT(10) UNSIGNED NULL,
  `speed` INT(10) UNSIGNED NULL,
  `url` VARCHAR(255) NULL
  COMMENT 'iqn for the servers',
  `fs_type` VARCHAR(32) NULL,
  `hypervisor_type` VARCHAR(32) NULL
  COMMENT 'hypervisor type, can be NONE for storage',
  `hypervisor_version` VARCHAR(32) NULL
  COMMENT 'hypervisor version',
  `ram` BIGINT NULL,
  `resource` VARCHAR(255) NULL
  COMMENT 'If it is a local resource, this is the class name',
  `version` VARCHAR(40) NOT NULL,
  `parent` VARCHAR(255) NULL
  COMMENT 'parent path for the storage server',
  `total_size` BIGINT NULL
  COMMENT 'TotalSize',
  `capabilities` VARCHAR(255) NULL
  COMMENT 'host capabilities in comma separated list',
  `guid` VARCHAR(255) NULL,
  `available` INT(1) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'Is this host ready for more resources?',
  `setup` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is this host already setup?',
  `dom0_memory` BIGINT NOT NULL
  COMMENT 'memory used by dom0 for computing and routing servers',
  `last_ping` INT(10) UNSIGNED NOT NULL
  COMMENT 'time in seconds from the start of machine of the last ping',
  `mgmt_server_id` BIGINT NULL
  COMMENT 'ManagementServer this host is connected to.',
  `disconnected` DATETIME NULL
  COMMENT 'Time this was disconnected',
  `created` DATETIME NULL
  COMMENT 'date the host first signed on',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `update_count` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'atomic increase count making status update operation atomical',
  `resource_state` VARCHAR(32) DEFAULT 'Enabled' NOT NULL
  COMMENT 'Is this host enabled for allocation for new resources',
  `owner` VARCHAR(255) NULL,
  `lastUpdated` DATETIME NULL
  COMMENT 'last updated',
  `engine_state` VARCHAR(32) DEFAULT 'Disabled' NOT NULL
  COMMENT 'the engine state of the zone',
  CONSTRAINT `uc_host__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `guid`
  UNIQUE (`guid`),
  CONSTRAINT `fk_host__cluster_id`
  FOREIGN KEY (`cluster_id`) REFERENCES `cloud`.`cluster` (`id`)
);

CREATE INDEX `i_host__cluster_id`
  ON `host` (`cluster_id`);

CREATE INDEX `i_host__data_center_id`
  ON `host` (`data_center_id`);

CREATE INDEX `i_host__last_ping`
  ON `host` (`last_ping`);

CREATE INDEX `i_host__pod_id`
  ON `host` (`pod_id`);

CREATE INDEX `i_host__removed`
  ON `host` (`removed`);

CREATE INDEX `i_host__status`
  ON `host` (`status`);

ALTER TABLE `dedicated_resources`
  ADD CONSTRAINT `fk_dedicated_resources__host_id`
FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`);

ALTER TABLE `external_load_balancer_devices`
  ADD CONSTRAINT `fk_external_lb_devices_host_id`
FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `external_load_balancer_devices`
  ADD CONSTRAINT `fk_external_lb_devices_parent_host_id`
FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `external_nicira_nvp_devices`
  ADD CONSTRAINT `fk_external_nicira_nvp_devices__host_id`
FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `external_nuage_vsp_devices`
  ADD CONSTRAINT `fk_external_nuage_vsp_devices__host_id`
FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `host_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `host_id` BIGINT NOT NULL
  COMMENT 'host id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  CONSTRAINT `uk_host_id_name`
  UNIQUE (`host_id`, `name`),
  CONSTRAINT `fk_host_details__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_host_details__host_id`
  ON `host_details` (`host_id`);

CREATE TABLE `host_gpu_groups` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `group_name` VARCHAR(255) NOT NULL,
  `host_id` BIGINT NOT NULL,
  CONSTRAINT `fk_host_gpu_groups__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_host_gpu_groups__host_id`
  ON `host_gpu_groups` (`host_id`);

CREATE TABLE `host_pod_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NULL,
  `uuid` VARCHAR(40) NULL,
  `data_center_id` BIGINT NOT NULL,
  `gateway` VARCHAR(255) NOT NULL
  COMMENT 'gateway for the pod',
  `cidr_address` VARCHAR(15) NOT NULL
  COMMENT 'CIDR address for the pod',
  `cidr_size` BIGINT NOT NULL
  COMMENT 'CIDR size for the pod',
  `description` VARCHAR(255) NULL
  COMMENT 'store private ip range in startIP-endIP format',
  `allocation_state` VARCHAR(32) DEFAULT 'Enabled' NOT NULL
  COMMENT 'Is this Pod enabled for allocation for new resources',
  `external_dhcp` TINYINT DEFAULT '0' NOT NULL
  COMMENT 'Is this Pod using external DHCP',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `owner` VARCHAR(255) NULL,
  `created` DATETIME NULL
  COMMENT 'date created',
  `lastUpdated` DATETIME NULL
  COMMENT 'last updated',
  `engine_state` VARCHAR(32) DEFAULT 'Disabled' NOT NULL
  COMMENT 'the engine state of the zone',
  CONSTRAINT `name`
  UNIQUE (`name`, `data_center_id`),
  CONSTRAINT `uc_host_pod_ref__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_host_pod_ref__allocation_state`
  ON `host_pod_ref` (`allocation_state`);

CREATE INDEX `i_host_pod_ref__data_center_id`
  ON `host_pod_ref` (`data_center_id`);

CREATE INDEX `i_host_pod_ref__removed`
  ON `host_pod_ref` (`removed`);

ALTER TABLE `cluster`
  ADD CONSTRAINT `fk_cluster__pod_id`
FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`);

ALTER TABLE `dc_storage_network_ip_range`
  ADD CONSTRAINT `fk_storage_ip_range__pod_id`
FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`);

ALTER TABLE `dedicated_resources`
  ADD CONSTRAINT `fk_dedicated_resources__pod_id`
FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`);

ALTER TABLE `host`
  ADD CONSTRAINT `fk_host__pod_id`
FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `host_tags` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `host_id` BIGINT NOT NULL
  COMMENT 'host id',
  `tag` VARCHAR(255) NOT NULL,
  CONSTRAINT `fk_host_tags__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_host_tags__host_id`
  ON `host_tags` (`host_id`);

CREATE TABLE `hypervisor_capabilities` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `hypervisor_type` VARCHAR(32) NOT NULL,
  `hypervisor_version` VARCHAR(32) NULL,
  `max_guests_limit` BIGINT DEFAULT '50' NULL,
  `security_group_enabled` INT(1) UNSIGNED DEFAULT '1' NULL
  COMMENT 'Is security group supported',
  `max_data_volumes_limit` INT(10) UNSIGNED DEFAULT '6' NULL
  COMMENT 'Max. data volumes per VM supported by hypervisor',
  `max_hosts_per_cluster` INT(10) UNSIGNED NULL
  COMMENT 'Max. hosts in cluster supported by hypervisor',
  `storage_motion_supported` INT(1) UNSIGNED DEFAULT '0' NULL
  COMMENT 'Is storage motion supported',
  `vm_snapshot_enabled` TINYINT(1) DEFAULT '0' NOT NULL
  COMMENT 'Whether VM snapshot is supported by hypervisor',
  CONSTRAINT `uc_hypervisor_capabilities__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `uc_hypervisor`
  UNIQUE (`hypervisor_type`, `hypervisor_version`)
);

CREATE TABLE `image_store` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL
  COMMENT 'name of data store',
  `image_provider_name` VARCHAR(255) NOT NULL
  COMMENT 'id of image_store_provider',
  `protocol` VARCHAR(255) NOT NULL
  COMMENT 'protocol of data store',
  `url` VARCHAR(2048) NULL,
  `data_center_id` BIGINT NULL
  COMMENT 'datacenter id of data store',
  `scope` VARCHAR(255) NULL
  COMMENT 'scope of data store',
  `role` VARCHAR(255) NULL
  COMMENT 'role of data store',
  `uuid` VARCHAR(255) NULL
  COMMENT 'uuid of data store',
  `parent` VARCHAR(255) NULL
  COMMENT 'parent path for the storage server',
  `created` DATETIME NULL
  COMMENT 'date the image store first signed on',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `total_size` BIGINT NULL
  COMMENT 'storage total size statistics',
  `used_bytes` BIGINT NULL
  COMMENT 'storage available bytes statistics'
);

CREATE TABLE `image_store_details` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `store_id` BIGINT NOT NULL
  COMMENT 'store the detail is related to',
  `name` VARCHAR(255) NOT NULL
  COMMENT 'name of the detail',
  `value` VARCHAR(255) NOT NULL
  COMMENT 'value of the detail',
  CONSTRAINT `fk_image_store_details__store_id`
  FOREIGN KEY (`store_id`) REFERENCES `cloud`.`image_store` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_image_store_details__store_id`
  ON `image_store_details` (`store_id`);

CREATE INDEX `i_image_store__name__value`
  ON `image_store_details` (`name`, `value`);

CREATE TABLE `inline_load_balancer_nic_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `public_ip_address` CHAR(40) NOT NULL,
  `nic_id` BIGINT NULL
  COMMENT 'nic id',
  CONSTRAINT `nic_id`
  UNIQUE (`nic_id`)
);

CREATE TABLE `instance_group` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `account_id` BIGINT NOT NULL
  COMMENT 'owner.  foreign key to account table',
  `name` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL,
  `removed` DATETIME NULL
  COMMENT 'date the group was removed',
  `created` DATETIME NULL
  COMMENT 'date the group was created',
  CONSTRAINT `uc_instance_group__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_instance_group__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
);

CREATE INDEX `i_instance_group__account_id`
  ON `instance_group` (`account_id`);

CREATE INDEX `i_instance_group__removed`
  ON `instance_group` (`removed`);

CREATE INDEX `i_name`
  ON `instance_group` (`name`);

CREATE TABLE `instance_group_vm_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `group_id` BIGINT NOT NULL,
  `instance_id` BIGINT NOT NULL,
  CONSTRAINT `fk_instance_group_vm_map___group_id`
  FOREIGN KEY (`group_id`) REFERENCES `cloud`.`instance_group` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_instance_group_vm_map___group_id`
  ON `instance_group_vm_map` (`group_id`);

CREATE INDEX `i_instance_group_vm_map___instance_id`
  ON `instance_group_vm_map` (`instance_id`);

CREATE TABLE `keystore` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `name` VARCHAR(64) NOT NULL
  COMMENT 'unique name for the certifiation',
  `certificate` TEXT NOT NULL
  COMMENT 'the actual certificate being stored in the db',
  `key` TEXT NULL
  COMMENT 'private key associated wih the certificate',
  `domain_suffix` VARCHAR(256) NOT NULL
  COMMENT 'DNS domain suffix associated with the certificate',
  `seq` INT NULL,
  CONSTRAINT `name`
  UNIQUE (`name`)
);

CREATE TABLE `launch_permission` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `template_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL
);

CREATE INDEX `i_launch_permission_template_id`
  ON `launch_permission` (`template_id`);

CREATE TABLE `ldap_configuration` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `hostname` VARCHAR(255) NOT NULL
  COMMENT 'the hostname of the ldap server',
  `port` INT(10) NULL
  COMMENT 'port that the ldap server is listening on'
);

CREATE TABLE `ldap_trust_map` (
  `id` INT(10) UNSIGNED AUTO_INCREMENT
    PRIMARY KEY,
  `domain_id` BIGINT NOT NULL,
  `type` VARCHAR(10) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `account_type` INT(1) UNSIGNED NOT NULL,
  CONSTRAINT `uk_ldap_trust_map__domain_id`
  UNIQUE (`domain_id`),
  CONSTRAINT `fk_ldap_trust_map__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
);

CREATE TABLE `legacy_zones` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `zone_id` BIGINT NOT NULL
  COMMENT 'id of CloudStack zone',
  CONSTRAINT `zone_id`
  UNIQUE (`zone_id`),
  CONSTRAINT `fk_legacy_zones__zone_id`
  FOREIGN KEY (`zone_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `load_balancer_cert_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `load_balancer_id` BIGINT NOT NULL,
  `certificate_id` BIGINT NOT NULL,
  `revoke` TINYINT(1) DEFAULT '0' NOT NULL
);

CREATE INDEX `i_load_balancer_cert_map__certificate_id`
  ON `load_balancer_cert_map` (`certificate_id`);

CREATE INDEX `i_load_balancer_cert_map__load_balancer_id`
  ON `load_balancer_cert_map` (`load_balancer_id`);

CREATE TABLE `load_balancer_healthcheck_policies` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `load_balancer_id` BIGINT NOT NULL,
  `pingpath` VARCHAR(225) DEFAULT '/' NULL,
  `description` VARCHAR(4096) NULL,
  `response_time` INT DEFAULT '5' NULL,
  `healthcheck_interval` INT DEFAULT '5' NULL,
  `healthcheck_thresshold` INT DEFAULT '2' NULL,
  `unhealth_thresshold` INT DEFAULT '10' NULL,
  `revoke` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 is when rule is set for Revoke',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the policy can be displayed to the end user',
  CONSTRAINT `id_UNIQUE`
  UNIQUE (`id`)
);

CREATE INDEX `i_load_balancer_healthcheck_policies_loadbalancer_id`
  ON `load_balancer_healthcheck_policies` (`load_balancer_id`);

CREATE TABLE `load_balancer_healthcheck_policy_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `lb_policy_id` BIGINT NOT NULL
  COMMENT 'resource id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_lb_healthcheck_policy_details__lb_healthcheck_policy_id`
  FOREIGN KEY (`lb_policy_id`) REFERENCES `cloud`.`load_balancer_healthcheck_policies` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_lb_healthcheck_policy_details__lb_healthcheck_policy_id`
  ON `load_balancer_healthcheck_policy_details` (`lb_policy_id`);

CREATE TABLE `load_balancer_stickiness_policies` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `load_balancer_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(4096) NULL
  COMMENT 'description',
  `method_name` VARCHAR(255) NOT NULL,
  `params` VARCHAR(4096) NOT NULL,
  `revoke` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 is when rule is set for Revoke',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the policy can be displayed to the end user'
);

CREATE INDEX `i_load_balancer_stickiness_policies__load_balancer_id`
  ON `load_balancer_stickiness_policies` (`load_balancer_id`);

CREATE TABLE `load_balancer_stickiness_policy_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `lb_policy_id` BIGINT NOT NULL
  COMMENT 'resource id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_lb_stickiness_policy_details__lb_stickiness_policy_id`
  FOREIGN KEY (`lb_policy_id`) REFERENCES `cloud`.`load_balancer_stickiness_policies` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_lb_stickiness_policy_details__lb_stickiness_policy_id`
  ON `load_balancer_stickiness_policy_details` (`lb_policy_id`);

CREATE TABLE `load_balancer_vm_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `load_balancer_id` BIGINT NOT NULL,
  `instance_id` BIGINT NOT NULL,
  `revoke` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 is when rule is set for Revoke',
  `state` VARCHAR(40) NULL
  COMMENT 'service status updated by LB healthcheck manager',
  `instance_ip` VARCHAR(40) NULL,
  CONSTRAINT `load_balancer_id`
  UNIQUE (`load_balancer_id`, `instance_id`, `instance_ip`)
);

CREATE INDEX `i_load_balancer_vm_map__instance_id`
  ON `load_balancer_vm_map` (`instance_id`);

CREATE TABLE `load_balancing_rules` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(4096) NULL
  COMMENT 'description',
  `default_port_start` INT(10) NOT NULL
  COMMENT 'default private port range start',
  `default_port_end` INT(10) NOT NULL
  COMMENT 'default destination port range end',
  `algorithm` VARCHAR(255) NOT NULL,
  `source_ip_address` VARCHAR(40) NULL
  COMMENT 'source ip address for the load balancer rule',
  `source_ip_address_network_id` BIGINT NULL
  COMMENT 'the id of the network where source ip belongs to',
  `scheme` VARCHAR(40) NOT NULL
  COMMENT 'load balancer scheme; can be Internal or Public',
  `lb_protocol` VARCHAR(40) NULL,
  `client_timeout` INT(10) NULL
  COMMENT 'client_timeout of haproxy config',
  `server_timeout` INT(10) NULL
  COMMENT 'server_timeout of haproxy config',
  CONSTRAINT `fk_load_balancing_rules__id`
  FOREIGN KEY (`id`) REFERENCES `cloud`.`firewall_rules` (`id`)
    ON DELETE CASCADE
);

ALTER TABLE `autoscale_vmgroups`
  ADD CONSTRAINT `fk_autoscale_vmgroup__load_balancer_id`
FOREIGN KEY (`load_balancer_id`) REFERENCES `cloud`.`load_balancing_rules` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `global_load_balancer_lb_rule_map`
  ADD CONSTRAINT `fk_lb_rule_id`
FOREIGN KEY (`lb_rule_id`) REFERENCES `cloud`.`load_balancing_rules` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `load_balancer_cert_map`
  ADD CONSTRAINT `fk_load_balancer_cert_map__load_balancer_id`
FOREIGN KEY (`load_balancer_id`) REFERENCES `cloud`.`load_balancing_rules` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `load_balancer_healthcheck_policies`
  ADD CONSTRAINT `fk_load_balancer_healthcheck_policies_loadbalancer_id`
FOREIGN KEY (`load_balancer_id`) REFERENCES `cloud`.`load_balancing_rules` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `load_balancer_stickiness_policies`
  ADD CONSTRAINT `fk_load_balancer_stickiness_policies__load_balancer_id`
FOREIGN KEY (`load_balancer_id`) REFERENCES `cloud`.`load_balancing_rules` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `load_balancer_vm_map`
  ADD CONSTRAINT `fk_load_balancer_vm_map__load_balancer_id`
FOREIGN KEY (`load_balancer_id`) REFERENCES `cloud`.`load_balancing_rules` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `monitoring_services` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `service` VARCHAR(255) NULL
  COMMENT 'Service name',
  `process_name` VARCHAR(255) NULL
  COMMENT 'running process name',
  `service_name` VARCHAR(255) NULL
  COMMENT 'exact name of the running service',
  `service_path` VARCHAR(255) NULL
  COMMENT 'path of the service in system',
  `pidfile` VARCHAR(255) NULL
  COMMENT 'path of the pid file of the service',
  `isDefault` TINYINT(1) NULL
  COMMENT 'Default service'
);

CREATE TABLE `mshost` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `msid` BIGINT NOT NULL
  COMMENT 'management server id derived from MAC address',
  `runid` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'run id, combined with msid to form a cluster session',
  `name` VARCHAR(255) NULL,
  `state` VARCHAR(10) DEFAULT 'Down' NOT NULL,
  `version` VARCHAR(255) NULL,
  `service_ip` CHAR(40) NOT NULL,
  `service_port` INT NOT NULL,
  `last_update` DATETIME NULL
  COMMENT 'Last record update time',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `alert_count` INT DEFAULT '0' NOT NULL,
  CONSTRAINT `msid`
  UNIQUE (`msid`)
);

CREATE INDEX `i_mshost__last_update`
  ON `mshost` (`last_update`);

CREATE INDEX `i_mshost__removed`
  ON `mshost` (`removed`);

CREATE TABLE `mshost_peer` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `owner_mshost` BIGINT NOT NULL,
  `peer_mshost` BIGINT NOT NULL,
  `peer_runid` BIGINT NOT NULL,
  `peer_state` VARCHAR(10) DEFAULT 'Down' NOT NULL,
  `last_update` DATETIME NULL
  COMMENT 'Last record update time',
  CONSTRAINT `i_mshost_peer__owner_peer_runid`
  UNIQUE (`owner_mshost`, `peer_mshost`, `peer_runid`),
  CONSTRAINT `fk_mshost_peer__owner_mshost`
  FOREIGN KEY (`owner_mshost`) REFERENCES `cloud`.`mshost` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_mshost_peer__peer_mshost`
  FOREIGN KEY (`peer_mshost`) REFERENCES `cloud`.`mshost` (`id`)
);

CREATE INDEX `i_mshost_peer__peer_mshost`
  ON `mshost_peer` (`peer_mshost`);

CREATE TABLE `network_acl` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL
  COMMENT 'name of the network acl',
  `uuid` VARCHAR(40) NULL,
  `vpc_id` BIGINT NULL
  COMMENT 'vpc this network acl belongs to',
  `description` VARCHAR(1024) NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the entry can be displayed to the end user'
);

CREATE TABLE `network_acl_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `network_acl_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_network_acl_details__network_acl_id`
  FOREIGN KEY (`network_acl_id`) REFERENCES `cloud`.`network_acl` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_network_acl_details__network_acl_id`
  ON `network_acl_details` (`network_acl_id`);

CREATE TABLE `network_acl_item` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `acl_id` BIGINT NOT NULL
  COMMENT 'network acl id',
  `start_port` INT(10) NULL
  COMMENT 'starting port of a port range',
  `end_port` INT(10) NULL
  COMMENT 'end port of a port range',
  `state` CHAR(32) NOT NULL
  COMMENT 'current state of this rule',
  `protocol` CHAR(16) DEFAULT 'TCP' NOT NULL
  COMMENT 'protocol to open these ports for',
  `created` DATETIME NULL
  COMMENT 'Date created',
  `icmp_code` INT(10) NULL
  COMMENT 'The ICMP code (if protocol=ICMP). A value of -1 means all codes for the given ICMP type.',
  `icmp_type` INT(10) NULL
  COMMENT 'The ICMP type (if protocol=ICMP). A value of -1 means all types.',
  `traffic_type` CHAR(32) NULL
  COMMENT 'the traffic type of the rule, can be Ingress or Egress',
  `number` INT(10) NOT NULL
  COMMENT 'priority number of the acl item',
  `action` VARCHAR(10) NOT NULL
  COMMENT 'rule action, allow or deny',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the entry can be displayed to the end user',
  CONSTRAINT `uc_network_acl_item__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `acl_id`
  UNIQUE (`acl_id`, `number`),
  CONSTRAINT `fk_network_acl_item__acl_id`
  FOREIGN KEY (`acl_id`) REFERENCES `cloud`.`network_acl` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `network_acl_item_cidrs` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `network_acl_item_id` BIGINT NOT NULL
  COMMENT 'Network ACL Item id',
  `cidr` VARCHAR(255) NOT NULL,
  CONSTRAINT `fk_network_acl_item_id`
  FOREIGN KEY (`network_acl_item_id`) REFERENCES `cloud`.`network_acl_item` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_network_acl_item_id`
  ON `network_acl_item_cidrs` (`network_acl_item_id`);

CREATE TABLE `network_acl_item_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `network_acl_item_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_network_acl_item_details__network_acl_item_id`
  FOREIGN KEY (`network_acl_item_id`) REFERENCES `cloud`.`network_acl_item` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_network_acl_item_details__network_acl_item_id`
  ON `network_acl_item_details` (`network_acl_item_id`);

CREATE TABLE `network_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `network_id` BIGINT NOT NULL
  COMMENT 'network id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_network_details__network_id`
  ON `network_details` (`network_id`);

CREATE TABLE `network_external_lb_device_map` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(255) NULL,
  `network_id` BIGINT NOT NULL
  COMMENT ' guest network id',
  `external_load_balancer_device_id` BIGINT NOT NULL
  COMMENT 'id of external load balancer device assigned for this network',
  `created` DATETIME NULL
  COMMENT 'Date from when network started using the device',
  `removed` DATETIME NULL
  COMMENT 'Date till the network stopped using the device',
  CONSTRAINT `uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_network_external_lb_devices_device_id`
  FOREIGN KEY (`external_load_balancer_device_id`) REFERENCES `cloud`.`external_load_balancer_devices` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_network_external_lb_devices_device_id`
  ON `network_external_lb_device_map` (`external_load_balancer_device_id`);

CREATE INDEX `i_network_external_lb_devices_network_id`
  ON `network_external_lb_device_map` (`network_id`);

CREATE TABLE `network_offering_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `network_offering_id` BIGINT NOT NULL
  COMMENT 'network offering id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL
);

CREATE INDEX `i_network_offering_details__network_offering_id`
  ON `network_offering_details` (`network_offering_id`);

CREATE TABLE `network_offerings` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `name` VARCHAR(64) NULL
  COMMENT 'name of the network offering',
  `uuid` VARCHAR(40) NULL,
  `unique_name` VARCHAR(64) NULL
  COMMENT 'unique name of the network offering',
  `display_text` VARCHAR(255) NOT NULL
  COMMENT 'text to display to users',
  `nw_rate` SMALLINT NULL
  COMMENT 'network rate throttle mbits/s',
  `mc_rate` SMALLINT NULL
  COMMENT 'mcast rate throttle mbits/s',
  `traffic_type` VARCHAR(32) NOT NULL
  COMMENT 'traffic type carried on this network',
  `tags` VARCHAR(4096) NULL
  COMMENT 'tags supported by this offering',
  `system_only` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is this network offering for system use only',
  `specify_vlan` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Should the user specify vlan',
  `service_offering_id` BIGINT NULL
  COMMENT 'service offering id that virtual router is tied to',
  `conserve_mode` INT(1) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'Is this network offering is IP conserve mode enabled',
  `created` DATETIME NOT NULL
  COMMENT 'time the entry was created',
  `removed` DATETIME NULL
  COMMENT 'time the entry was removed',
  `default` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if network offering is default',
  `availability` VARCHAR(255) NOT NULL
  COMMENT 'availability of the network',
  `dedicated_lb_service` INT(1) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'true if the network offering provides a dedicated load balancer for each network',
  `shared_source_nat_service` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering provides the shared source nat service',
  `sort_key` INT(32) DEFAULT '0' NOT NULL
  COMMENT 'sort key used for customising sort method',
  `redundant_router_service` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering provides the redundant router service',
  `state` CHAR(32) NULL
  COMMENT 'state of the network offering that has Disabled value by default',
  `guest_type` CHAR(32) NULL
  COMMENT 'type of guest network that can be shared or isolated',
  `elastic_ip_service` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering provides elastic ip service',
  `eip_associate_public_ip` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if public IP is associated with user VM creation by default when EIP service is enabled.',
  `elastic_lb_service` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering provides elastic lb service',
  `specify_ip_ranges` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering provides an ability to define ip ranges',
  `inline` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is this network offering LB provider is in inline mode',
  `is_persistent` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering provides an ability to create persistent networks',
  `internal_lb` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering supports Internal lb service',
  `public_lb` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network offering supports Public lb service',
  `egress_default_policy` TINYINT(1) DEFAULT '0' NULL,
  `concurrent_connections` INT(10) UNSIGNED NULL
  COMMENT 'Load Balancer(haproxy) maximum number of concurrent connections(global max)',
  `keep_alive_enabled` INT(1) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'true if connection should be reset after requests.',
  `supports_streched_l2` TINYINT(1) DEFAULT '0' NULL,
  `secondary_service_offering_id` BIGINT NULL
  COMMENT 'service offering id that a secondary virtual router is tied to',
  CONSTRAINT `uc_network_offerings__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `unique_name`
  UNIQUE (`unique_name`)
);

CREATE INDEX `i_network_offerings__removed`
  ON `network_offerings` (`removed`);

CREATE INDEX `i_network_offerings__system_only`
  ON `network_offerings` (`system_only`);

ALTER TABLE `network_offering_details`
  ADD CONSTRAINT `fk_network_offering_details__network_offering_id`
FOREIGN KEY (`network_offering_id`) REFERENCES `cloud`.`network_offerings` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `network_rule_config` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `security_group_id` BIGINT NOT NULL,
  `public_port` VARCHAR(10) NULL,
  `private_port` VARCHAR(10) NULL,
  `protocol` VARCHAR(16) DEFAULT 'TCP' NOT NULL,
  `create_status` VARCHAR(32) NULL
  COMMENT 'rule creation status'
);

CREATE TABLE `networks` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `name` VARCHAR(255) NULL
  COMMENT 'name for this network',
  `uuid` VARCHAR(40) NULL,
  `display_text` VARCHAR(255) NULL
  COMMENT 'display text for this network',
  `traffic_type` VARCHAR(32) NOT NULL
  COMMENT 'type of traffic going through this network',
  `broadcast_domain_type` VARCHAR(32) NOT NULL
  COMMENT 'type of broadcast domain used',
  `broadcast_uri` VARCHAR(255) NULL
  COMMENT 'broadcast domain specifier',
  `gateway` VARCHAR(15) NULL
  COMMENT 'gateway for this network configuration',
  `cidr` VARCHAR(18) NULL
  COMMENT 'CloudStack managed vms get IP address from cidr.In general this cidr also serves as the network CIDR. But in case IP reservation feature is being used by a Guest network, networkcidr is the Effective network CIDR for that network',
  `mode` VARCHAR(32) NULL
  COMMENT 'How to retrieve ip address in this network',
  `network_offering_id` BIGINT NOT NULL
  COMMENT 'network offering id that this configuration is created from',
  `physical_network_id` BIGINT NULL
  COMMENT 'physical network id that this configuration is based on',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center id that this configuration is used in',
  `guru_name` VARCHAR(255) NOT NULL
  COMMENT 'who is responsible for this type of network configuration',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'what state is this configuration in',
  `related` BIGINT NOT NULL
  COMMENT 'related to what other network configuration',
  `domain_id` BIGINT NOT NULL
  COMMENT 'foreign key to domain id',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner of this network',
  `dns1` VARCHAR(255) NULL
  COMMENT 'comma separated DNS list',
  `dns2` VARCHAR(255) NULL
  COMMENT 'comma separated DNS list',
  `guru_data` VARCHAR(1024) NULL
  COMMENT 'data stored by the network guru that setup this network',
  `set_fields` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'which fields are set already',
  `acl_type` VARCHAR(15) NULL
  COMMENT 'ACL access type. Null for system networks, can be Account/Domain for Guest networks',
  `network_domain` VARCHAR(255) NULL
  COMMENT 'domain',
  `reservation_id` CHAR(40) NULL
  COMMENT 'reservation id',
  `guest_type` CHAR(32) NULL
  COMMENT 'type of guest network that can be shared or isolated',
  `restart_required` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if restart is required for the network',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `specify_ip_ranges` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network provides an ability to define ip ranges',
  `vpc_id` BIGINT NULL
  COMMENT 'vpc this network belongs to',
  `ip6_gateway` VARCHAR(50) NULL
  COMMENT 'IPv6 gateway for this network',
  `ip6_cidr` VARCHAR(50) NULL
  COMMENT 'IPv6 cidr for this network',
  `network_cidr` VARCHAR(18) NULL
  COMMENT 'The network cidr for the isolated guest network which uses IP Reservation facility.For networks not using IP reservation, network_cidr is always null.',
  `display_network` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'Should network be displayed to the end user',
  `network_acl_id` BIGINT NULL
  COMMENT 'network acl id',
  `streched_l2` TINYINT(1) DEFAULT '0' NULL,
  `redundant` TINYINT(1) DEFAULT '0' NULL,
  `ip_exclusion_list` VARCHAR(255) NULL
  COMMENT 'IP list excluded from assignment',
  CONSTRAINT `uc_networks__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_networks__network_offering_id`
  FOREIGN KEY (`network_offering_id`) REFERENCES `cloud`.`network_offerings` (`id`),
  CONSTRAINT `fk_networks__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_networks__related`
  FOREIGN KEY (`related`) REFERENCES `cloud`.`networks` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_networks__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`),
  CONSTRAINT `fk_networks__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
);

CREATE INDEX `i_networks__account_id`
  ON `networks` (`account_id`);

CREATE INDEX `i_networks__data_center_id`
  ON `networks` (`data_center_id`);

CREATE INDEX `i_networks__domain_id`
  ON `networks` (`domain_id`);

CREATE INDEX `i_networks__network_offering_id`
  ON `networks` (`network_offering_id`);

CREATE INDEX `i_networks__related`
  ON `networks` (`related`);

CREATE INDEX `i_networks__vpc_id`
  ON `networks` (`vpc_id`);

CREATE INDEX `i_networks__removed`
  ON `networks` (`removed`);

ALTER TABLE `account_network_ref`
  ADD CONSTRAINT `fk_account_network_ref__networks_id`
FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `dc_storage_network_ip_range`
  ADD CONSTRAINT `fk_storage_ip_range__network_id`
FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`);

ALTER TABLE `domain_network_ref`
  ADD CONSTRAINT `fk_domain_network_ref__networks_id`
FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `firewall_rules`
  ADD CONSTRAINT `fk_firewall_rules__network_id`
FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `network_details`
  ADD CONSTRAINT `fk_network_details__network_id`
FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `network_external_lb_device_map`
  ADD CONSTRAINT `fk_network_external_lb_devices_network_id`
FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `networks_pre520` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `name` VARCHAR(255) NULL
  COMMENT 'name for this network',
  `uuid` VARCHAR(40) NULL,
  `display_text` VARCHAR(255) NULL
  COMMENT 'display text for this network',
  `traffic_type` VARCHAR(32) NOT NULL
  COMMENT 'type of traffic going through this network',
  `broadcast_domain_type` VARCHAR(32) NOT NULL
  COMMENT 'type of broadcast domain used',
  `broadcast_uri` VARCHAR(255) NULL
  COMMENT 'broadcast domain specifier',
  `gateway` VARCHAR(15) NULL
  COMMENT 'gateway for this network configuration',
  `cidr` VARCHAR(18) NULL
  COMMENT 'CloudStack managed vms get IP address from cidr.In general this cidr also serves as the network CIDR. But in case IP reservation feature is being used by a Guest network, networkcidr is the Effective network CIDR for that network',
  `mode` VARCHAR(32) NULL
  COMMENT 'How to retrieve ip address in this network',
  `network_offering_id` BIGINT NOT NULL
  COMMENT 'network offering id that this configuration is created from',
  `physical_network_id` BIGINT NULL
  COMMENT 'physical network id that this configuration is based on',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center id that this configuration is used in',
  `guru_name` VARCHAR(255) NOT NULL
  COMMENT 'who is responsible for this type of network configuration',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'what state is this configuration in',
  `related` BIGINT NOT NULL
  COMMENT 'related to what other network configuration',
  `domain_id` BIGINT NOT NULL
  COMMENT 'foreign key to domain id',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner of this network',
  `dns1` VARCHAR(255) NULL
  COMMENT 'comma separated DNS list',
  `dns2` VARCHAR(255) NULL
  COMMENT 'comma separated DNS list',
  `guru_data` VARCHAR(1024) NULL
  COMMENT 'data stored by the network guru that setup this network',
  `set_fields` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'which fields are set already',
  `acl_type` VARCHAR(15) NULL
  COMMENT 'ACL access type. Null for system networks, can be Account/Domain for Guest networks',
  `network_domain` VARCHAR(255) NULL
  COMMENT 'domain',
  `reservation_id` CHAR(40) NULL
  COMMENT 'reservation id',
  `guest_type` CHAR(32) NULL
  COMMENT 'type of guest network that can be shared or isolated',
  `restart_required` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if restart is required for the network',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `specify_ip_ranges` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the network provides an ability to define ip ranges',
  `vpc_id` BIGINT NULL
  COMMENT 'vpc this network belongs to',
  `ip6_gateway` VARCHAR(50) NULL
  COMMENT 'IPv6 gateway for this network',
  `ip6_cidr` VARCHAR(50) NULL
  COMMENT 'IPv6 cidr for this network',
  `network_cidr` VARCHAR(18) NULL
  COMMENT 'The network cidr for the isolated guest network which uses IP Reservation facility.For networks not using IP reservation, network_cidr is always null.',
  `display_network` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'Should network be displayed to the end user',
  `network_acl_id` BIGINT NULL
  COMMENT 'network acl id',
  `streched_l2` TINYINT(1) DEFAULT '0' NULL,
  `redundant` TINYINT(1) DEFAULT '0' NULL,
  CONSTRAINT `uc_networks__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_networks__account_id`
  ON `networks_pre520` (`account_id`);

CREATE INDEX `i_networks__data_center_id`
  ON `networks_pre520` (`data_center_id`);

CREATE INDEX `i_networks__domain_id`
  ON `networks_pre520` (`domain_id`);

CREATE INDEX `i_networks__network_offering_id`
  ON `networks_pre520` (`network_offering_id`);

CREATE INDEX `i_networks__related`
  ON `networks_pre520` (`related`);

CREATE INDEX `i_networks__vpc_id`
  ON `networks_pre520` (`vpc_id`);

CREATE INDEX `i_networks__removed`
  ON `networks_pre520` (`removed`);

CREATE TABLE `nic_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `nic_id` BIGINT NOT NULL
  COMMENT 'nic id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_nic_details__nic_id`
  ON `nic_details` (`nic_id`);

CREATE TABLE `nic_ip_alias` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NOT NULL,
  `nic_id` BIGINT NULL,
  `ip4_address` CHAR(40) NULL,
  `ip6_address` CHAR(40) NULL,
  `netmask` CHAR(40) NULL,
  `gateway` CHAR(40) NULL,
  `start_ip_of_subnet` CHAR(40) NULL,
  `network_id` BIGINT NULL,
  `vmId` BIGINT NULL,
  `alias_count` BIGINT NULL,
  `created` DATETIME NOT NULL,
  `account_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `state` CHAR(32) NOT NULL,
  CONSTRAINT `id_UNIQUE`
  UNIQUE (`id`)
);

CREATE TABLE `nic_secondary_ips` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `vmId` BIGINT NULL
  COMMENT 'vm instance id',
  `nicId` BIGINT NOT NULL,
  `ip4_address` CHAR(40) NULL
  COMMENT 'ip4 address',
  `ip6_address` CHAR(40) NULL
  COMMENT 'ip6 address',
  `network_id` BIGINT NOT NULL
  COMMENT 'network configuration id',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner.  foreign key to   account table',
  `domain_id` BIGINT NOT NULL
  COMMENT 'the domain that the owner belongs to',
  CONSTRAINT `uc_nic_secondary_ip__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_nic_secondary_ip__networks_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
);

CREATE INDEX `i_nic_secondary_ip__networks_id`
  ON `nic_secondary_ips` (`network_id`);

CREATE INDEX `i_nic_secondary_ip__vmId`
  ON `nic_secondary_ips` (`vmId`);

CREATE TABLE `nicira_nvp_nic_map` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `logicalswitch` VARCHAR(255) NOT NULL
  COMMENT 'nicira uuid of logical switch this port is provisioned on',
  `logicalswitchport` VARCHAR(255) NULL
  COMMENT 'nicira uuid of this logical switch port',
  `nic` VARCHAR(255) NULL
  COMMENT 'cloudstack uuid of the nic connected to this logical switch port',
  CONSTRAINT `logicalswitchport`
  UNIQUE (`logicalswitchport`),
  CONSTRAINT `nic`
  UNIQUE (`nic`)
);

CREATE TABLE `nicira_nvp_router_map` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `logicalrouter_uuid` VARCHAR(255) NOT NULL
  COMMENT 'nicira uuid of logical router',
  `network_id` BIGINT NOT NULL
  COMMENT 'cloudstack id of the network',
  CONSTRAINT `logicalrouter_uuid`
  UNIQUE (`logicalrouter_uuid`),
  CONSTRAINT `network_id`
  UNIQUE (`network_id`),
  CONSTRAINT `fk_nicira_nvp_router_map__network_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `nics` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `instance_id` BIGINT NULL
  COMMENT 'vm instance id',
  `mac_address` VARCHAR(17) NULL
  COMMENT 'mac address',
  `ip4_address` CHAR(40) NULL
  COMMENT 'ip4 address',
  `netmask` VARCHAR(15) NULL
  COMMENT 'netmask for ip4 address',
  `gateway` VARCHAR(15) NULL
  COMMENT 'gateway',
  `ip_type` VARCHAR(32) NULL
  COMMENT 'type of ip',
  `broadcast_uri` VARCHAR(255) NULL
  COMMENT 'broadcast uri',
  `network_id` BIGINT NOT NULL
  COMMENT 'network configuration id',
  `mode` VARCHAR(32) NULL
  COMMENT 'mode of getting ip address',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'state of the creation',
  `strategy` VARCHAR(32) NOT NULL
  COMMENT 'reservation strategy',
  `reserver_name` VARCHAR(255) NULL
  COMMENT 'Name of the component that reserved the ip address',
  `reservation_id` VARCHAR(64) NULL
  COMMENT 'id for the reservation',
  `device_id` INT(10) NULL
  COMMENT 'device id for the network when plugged into the virtual machine',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
  COMMENT 'time the state was changed',
  `isolation_uri` VARCHAR(255) NULL
  COMMENT 'id for isolation',
  `ip6_address` CHAR(40) NULL
  COMMENT 'ip6 address',
  `default_nic` TINYINT NOT NULL
  COMMENT 'None',
  `vm_type` VARCHAR(32) NULL
  COMMENT 'type of vm: System or User vm',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `ip6_gateway` VARCHAR(50) NULL
  COMMENT 'gateway for ip6 address',
  `ip6_cidr` VARCHAR(50) NULL
  COMMENT 'cidr for ip6 address',
  `secondary_ip` SMALLINT(6) DEFAULT '0' NULL
  COMMENT 'secondary ips configured for the nic',
  `display_nic` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'Should nic be displayed to the end user',
  CONSTRAINT `uc_nics__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_nics__networks_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
);

CREATE INDEX `i_nics__instance_id`
  ON `nics` (`instance_id`);

CREATE INDEX `i_nics__networks_id`
  ON `nics` (`network_id`);

CREATE INDEX `i_nics__removed`
  ON `nics` (`removed`);

ALTER TABLE `inline_load_balancer_nic_map`
  ADD CONSTRAINT `fk_inline_load_balancer_nic_map__nic_id`
FOREIGN KEY (`nic_id`) REFERENCES `cloud`.`nics` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `nic_details`
  ADD CONSTRAINT `fk_nic_details__nic_id`
FOREIGN KEY (`nic_id`) REFERENCES `cloud`.`nics` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `nicira_nvp_nic_map`
  ADD CONSTRAINT `fk_nicira_nvp_nic_map__nic`
FOREIGN KEY (`nic`) REFERENCES `cloud`.`nics` (`uuid`)
  ON DELETE CASCADE;

CREATE TABLE `ntwk_offering_service_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `network_offering_id` BIGINT NOT NULL
  COMMENT 'network_offering_id',
  `service` VARCHAR(255) NOT NULL
  COMMENT 'service',
  `provider` VARCHAR(255) NULL
  COMMENT 'service provider',
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `network_offering_id`
  UNIQUE (`network_offering_id`, `service`, `provider`),
  CONSTRAINT `fk_ntwk_offering_service_map__network_offering_id`
  FOREIGN KEY (`network_offering_id`) REFERENCES `cloud`.`network_offerings` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `ntwk_service_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `network_id` BIGINT NOT NULL
  COMMENT 'network_id',
  `service` VARCHAR(255) NOT NULL
  COMMENT 'service',
  `provider` VARCHAR(255) NULL
  COMMENT 'service provider',
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `network_id`
  UNIQUE (`network_id`, `service`, `provider`),
  CONSTRAINT `fk_ntwk_service_map__network_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `object_datastore_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `datastore_uuid` VARCHAR(255) NOT NULL,
  `datastore_role` VARCHAR(255) NOT NULL,
  `object_uuid` VARCHAR(255) NOT NULL,
  `object_type` VARCHAR(255) NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `download_pct` INT(10) UNSIGNED NULL,
  `download_state` VARCHAR(255) NULL,
  `url` VARCHAR(2048) NULL,
  `format` VARCHAR(255) NULL,
  `checksum` VARCHAR(255) NULL,
  `error_str` VARCHAR(255) NULL,
  `local_path` VARCHAR(255) NULL,
  `install_path` VARCHAR(255) NULL,
  `size` BIGINT NULL
  COMMENT 'the size of the template on the pool',
  `state` VARCHAR(255) NOT NULL,
  `update_count` BIGINT NOT NULL,
  `updated` DATETIME NULL
);

CREATE TABLE `op_dc_ip_address_alloc` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'primary key'
    PRIMARY KEY,
  `ip_address` CHAR(40) NOT NULL
  COMMENT 'ip address',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center it belongs to',
  `pod_id` BIGINT NOT NULL
  COMMENT 'pod it belongs to',
  `nic_id` BIGINT NULL
  COMMENT 'nic id',
  `reservation_id` CHAR(40) NULL
  COMMENT 'reservation id',
  `taken` DATETIME NULL
  COMMENT 'Date taken',
  `mac_address` BIGINT NOT NULL
  COMMENT 'mac address for management ips',
  CONSTRAINT `i_op_dc_ip_address_alloc__ip_address__data_center_id`
  UNIQUE (`ip_address`, `data_center_id`),
  CONSTRAINT `fk_op_dc_ip_address_alloc__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_op_dc_ip_address_alloc__pod_id`
  FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_op_dc_ip_address_alloc__data_center_id`
  ON `op_dc_ip_address_alloc` (`data_center_id`);

CREATE INDEX `i_op_dc_ip_address_alloc__pod_id`
  ON `op_dc_ip_address_alloc` (`pod_id`);

CREATE INDEX `i_op_dc_ip_address_alloc__pod_id__data_center_id__taken`
  ON `op_dc_ip_address_alloc` (`pod_id`, `data_center_id`, `taken`, `nic_id`);

CREATE TABLE `op_dc_link_local_ip_address_alloc` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'primary key'
    PRIMARY KEY,
  `ip_address` CHAR(40) NOT NULL
  COMMENT 'ip address',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center it belongs to',
  `pod_id` BIGINT NOT NULL
  COMMENT 'pod it belongs to',
  `nic_id` BIGINT NULL
  COMMENT 'instance id',
  `reservation_id` CHAR(40) NULL
  COMMENT 'reservation id used to reserve this network',
  `taken` DATETIME NULL
  COMMENT 'Date taken'
);

CREATE INDEX `i_op_dc_link_local_ip_address_alloc__data_center_id`
  ON `op_dc_link_local_ip_address_alloc` (`data_center_id`);

CREATE INDEX `i_op_dc_link_local_ip_address_alloc__nic_id_reservation_id`
  ON `op_dc_link_local_ip_address_alloc` (`nic_id`, `reservation_id`);

CREATE INDEX `i_op_dc_link_local_ip_address_alloc__pod_id`
  ON `op_dc_link_local_ip_address_alloc` (`pod_id`);

CREATE TABLE `op_dc_storage_network_ip_address` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'primary key'
    PRIMARY KEY,
  `range_id` BIGINT NOT NULL
  COMMENT 'id of ip range in dc_storage_network_ip_range',
  `ip_address` CHAR(40) NOT NULL
  COMMENT 'ip address',
  `mac_address` BIGINT NOT NULL
  COMMENT 'mac address for storage ips',
  `taken` DATETIME NULL
  COMMENT 'Date taken',
  CONSTRAINT `fk_storage_ip_address__range_id`
  FOREIGN KEY (`range_id`) REFERENCES `cloud`.`dc_storage_network_ip_range` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_storage_ip_address__range_id`
  ON `op_dc_storage_network_ip_address` (`range_id`);

CREATE TABLE `op_dc_vnet_alloc` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'primary id'
    PRIMARY KEY,
  `vnet` VARCHAR(18) NOT NULL
  COMMENT 'vnet',
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'physical network the vnet belongs to',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center the vnet belongs to',
  `reservation_id` CHAR(40) NULL
  COMMENT 'reservation id',
  `account_id` BIGINT NULL
  COMMENT 'account the vnet belongs to right now',
  `taken` DATETIME NULL
  COMMENT 'Date taken',
  `account_vnet_map_id` BIGINT NULL,
  CONSTRAINT `i_op_dc_vnet_alloc__vnet__data_center_id`
  UNIQUE (`vnet`, `physical_network_id`, `data_center_id`),
  CONSTRAINT `fk_op_dc_vnet_alloc__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_op_dc_vnet_alloc__account_vnet_map_id`
  FOREIGN KEY (`account_vnet_map_id`) REFERENCES `cloud`.`account_vnet_map` (`id`)
);

CREATE INDEX `i_op_dc_vnet_alloc__account_vnet_map_id`
  ON `op_dc_vnet_alloc` (`account_vnet_map_id`);

CREATE INDEX `i_op_dc_vnet_alloc__physical_network_id`
  ON `op_dc_vnet_alloc` (`physical_network_id`);

CREATE INDEX `i_op_dc_vnet_alloc__dc_taken`
  ON `op_dc_vnet_alloc` (`data_center_id`, `taken`);

CREATE TABLE `op_ha_work` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `instance_id` BIGINT NOT NULL
  COMMENT 'vm instance that needs to be ha.',
  `type` VARCHAR(32) NOT NULL
  COMMENT 'type of work',
  `vm_type` VARCHAR(32) NOT NULL
  COMMENT 'VM type',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'state of the vm instance when this happened.',
  `mgmt_server_id` BIGINT NULL
  COMMENT 'management server that has taken up the work of doing ha',
  `host_id` BIGINT NULL
  COMMENT 'host that the vm is suppose to be on',
  `created` DATETIME NOT NULL
  COMMENT 'time the entry was requested',
  `tried` INT(10) UNSIGNED NULL
  COMMENT '# of times tried',
  `taken` DATETIME NULL
  COMMENT 'time it was taken by the management server',
  `step` VARCHAR(32) NOT NULL
  COMMENT 'Step in the work',
  `time_to_try` BIGINT NULL
  COMMENT 'time to try do this work',
  `updated` BIGINT NOT NULL
  COMMENT 'time the VM state was updated when it was stored into work queue',
  CONSTRAINT `fk_op_ha_work__mgmt_server_id`
  FOREIGN KEY (`mgmt_server_id`) REFERENCES `cloud`.`mshost` (`msid`),
  CONSTRAINT `fk_op_ha_work__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
);

CREATE INDEX `i_op_ha_work__host_id`
  ON `op_ha_work` (`host_id`);

CREATE INDEX `i_op_ha_work__instance_id`
  ON `op_ha_work` (`instance_id`);

CREATE INDEX `i_op_ha_work__mgmt_server_id`
  ON `op_ha_work` (`mgmt_server_id`);

CREATE INDEX `i_op_ha_work__step`
  ON `op_ha_work` (`step`);

CREATE INDEX `i_op_ha_work__type`
  ON `op_ha_work` (`type`);

CREATE TABLE `op_host` (
  `id` BIGINT NOT NULL
  COMMENT 'host id'
    PRIMARY KEY,
  `sequence` BIGINT DEFAULT '1' NOT NULL
  COMMENT 'sequence for the host communication',
  CONSTRAINT `fk_op_host__id`
  FOREIGN KEY (`id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `op_host_capacity` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `host_id` BIGINT NULL,
  `data_center_id` BIGINT NOT NULL,
  `pod_id` BIGINT NULL,
  `cluster_id` BIGINT NULL
  COMMENT 'foreign key to cluster',
  `used_capacity` BIGINT NOT NULL,
  `reserved_capacity` BIGINT NOT NULL,
  `total_capacity` BIGINT NOT NULL,
  `capacity_type` INT(1) UNSIGNED NOT NULL,
  `capacity_state` VARCHAR(32) DEFAULT 'Enabled' NOT NULL
  COMMENT 'Is this capacity enabled for allocation for new resources',
  `update_time` DATETIME NULL
  COMMENT 'time the capacity was last updated',
  `created` DATETIME NULL
  COMMENT 'date created'
);

CREATE INDEX `i_op_host_capacity__cluster_id`
  ON `op_host_capacity` (`cluster_id`);

CREATE INDEX `i_op_host_capacity__data_center_id`
  ON `op_host_capacity` (`data_center_id`);

CREATE INDEX `i_op_host_capacity__host_type`
  ON `op_host_capacity` (`host_id`, `capacity_type`);

CREATE INDEX `i_op_host_capacity__pod_id`
  ON `op_host_capacity` (`pod_id`);

CREATE TABLE `op_host_planner_reservation` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `data_center_id` BIGINT NOT NULL,
  `pod_id` BIGINT NULL,
  `cluster_id` BIGINT NULL,
  `host_id` BIGINT NULL,
  `resource_usage` VARCHAR(255) NULL
  COMMENT 'Shared(between planners) Vs Dedicated (exclusive usage to a planner)',
  CONSTRAINT `fk_planner_reservation__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_planner_reservation__pod_id`
  FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_planner_reservation__cluster_id`
  FOREIGN KEY (`cluster_id`) REFERENCES `cloud`.`cluster` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_planner_reservation__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_planner_reservation__cluster_id`
  ON `op_host_planner_reservation` (`cluster_id`);

CREATE INDEX `i_planner_reservation__data_center_id`
  ON `op_host_planner_reservation` (`data_center_id`);

CREATE INDEX `i_planner_reservation__pod_id`
  ON `op_host_planner_reservation` (`pod_id`);

CREATE INDEX `i_op_host_planner_reservation__host_resource_usage`
  ON `op_host_planner_reservation` (`host_id`, `resource_usage`);

CREATE TABLE `op_host_transfer` (
  `id` BIGINT NOT NULL
  COMMENT 'Id of the host'
    PRIMARY KEY,
  `initial_mgmt_server_id` BIGINT NULL
  COMMENT 'management server the host is transfered from',
  `future_mgmt_server_id` BIGINT NULL
  COMMENT 'management server the host is transfered to',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'the transfer state of the host',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  CONSTRAINT `fk_op_host_transfer__id`
  FOREIGN KEY (`id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_op_host_transfer__initial_mgmt_server_id`
  FOREIGN KEY (`initial_mgmt_server_id`) REFERENCES `cloud`.`mshost` (`msid`),
  CONSTRAINT `fk_op_host_transfer__future_mgmt_server_id`
  FOREIGN KEY (`future_mgmt_server_id`) REFERENCES `cloud`.`mshost` (`msid`)
);

CREATE INDEX `i_op_host_transfer__future_mgmt_server_id`
  ON `op_host_transfer` (`future_mgmt_server_id`);

CREATE INDEX `i_op_host_transfer__initial_mgmt_server_id`
  ON `op_host_transfer` (`initial_mgmt_server_id`);

CREATE TABLE `op_host_upgrade` (
  `host_id` BIGINT NOT NULL
  COMMENT 'host id'
    PRIMARY KEY,
  `version` VARCHAR(20) NOT NULL
  COMMENT 'version',
  `state` VARCHAR(20) NOT NULL
  COMMENT 'state',
  CONSTRAINT `host_id`
  UNIQUE (`host_id`)
);

CREATE TABLE `op_it_work` (
  `id` CHAR(40) DEFAULT '' NOT NULL
  COMMENT 'reservation id'
    PRIMARY KEY,
  `mgmt_server_id` BIGINT NULL
  COMMENT 'management server id',
  `created_at` BIGINT NOT NULL
  COMMENT 'when was this work detail created',
  `thread` VARCHAR(255) NOT NULL
  COMMENT 'thread name',
  `type` CHAR(32) NOT NULL
  COMMENT 'type of work',
  `vm_type` CHAR(32) NOT NULL
  COMMENT 'type of vm',
  `step` CHAR(32) NOT NULL
  COMMENT 'state',
  `updated_at` BIGINT NOT NULL
  COMMENT 'time it was taken over',
  `instance_id` BIGINT NOT NULL
  COMMENT 'vm instance',
  `resource_type` CHAR(32) NULL
  COMMENT 'type of resource being worked on',
  `resource_id` BIGINT NULL
  COMMENT 'resource id being worked on',
  CONSTRAINT `fk_op_it_work__mgmt_server_id`
  FOREIGN KEY (`mgmt_server_id`) REFERENCES `cloud`.`mshost` (`msid`)
);

CREATE INDEX `i_op_it_work__instance_id`
  ON `op_it_work` (`instance_id`);

CREATE INDEX `i_op_it_work__mgmt_server_id`
  ON `op_it_work` (`mgmt_server_id`);

CREATE INDEX `i_op_it_work__step`
  ON `op_it_work` (`step`);

CREATE INDEX `i_type_and_updated`
  ON `op_it_work` (`type`, `updated_at`);

CREATE TABLE `op_lock` (
  `key` VARCHAR(128) NOT NULL
  COMMENT 'primary key of the table'
    PRIMARY KEY,
  `mac` VARCHAR(17) NOT NULL
  COMMENT 'management server id of the server that holds this lock',
  `ip` CHAR(40) NOT NULL
  COMMENT 'name of the thread that holds this lock',
  `thread` VARCHAR(255) NOT NULL
  COMMENT 'Thread id that acquired this lock',
  `acquired_on` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
  COMMENT 'Time acquired',
  `waiters` INT DEFAULT '0' NOT NULL
  COMMENT 'How many have the thread acquired this lock (reentrant)',
  CONSTRAINT `key`
  UNIQUE (`key`)
);

CREATE INDEX `i_op_lock__mac_ip_thread`
  ON `op_lock` (`mac`, `ip`, `thread`);

CREATE TABLE `op_networks` (
  `id` BIGINT NOT NULL
    PRIMARY KEY,
  `mac_address_seq` BIGINT DEFAULT '1' NOT NULL
  COMMENT 'mac address',
  `nics_count` INT(10) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '# of nics',
  `gc` TINYINT(3) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'gc this network or not',
  `check_for_gc` TINYINT(3) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'check this network for gc or not',
  CONSTRAINT `fk_op_networks__id`
  FOREIGN KEY (`id`) REFERENCES `cloud`.`networks` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `op_nwgrp_work` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `instance_id` BIGINT NOT NULL
  COMMENT 'vm instance that needs rules to be synced.',
  `mgmt_server_id` BIGINT NULL
  COMMENT 'management server that has taken up the work of doing rule sync',
  `created` DATETIME NOT NULL
  COMMENT 'time the entry was requested',
  `taken` DATETIME NULL
  COMMENT 'time it was taken by the management server',
  `step` VARCHAR(32) NOT NULL
  COMMENT 'Step in the work',
  `seq_no` BIGINT NULL
  COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update'
);

CREATE INDEX `i_op_nwgrp_work__instance_id`
  ON `op_nwgrp_work` (`instance_id`);

CREATE INDEX `i_op_nwgrp_work__mgmt_server_id`
  ON `op_nwgrp_work` (`mgmt_server_id`);

CREATE INDEX `i_op_nwgrp_work__seq_no`
  ON `op_nwgrp_work` (`seq_no`);

CREATE INDEX `i_op_nwgrp_work__step`
  ON `op_nwgrp_work` (`step`);

CREATE INDEX `i_op_nwgrp_work__taken`
  ON `op_nwgrp_work` (`taken`);

CREATE TABLE `op_pod_vlan_alloc` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'primary id'
    PRIMARY KEY,
  `vlan` VARCHAR(18) NOT NULL
  COMMENT 'vlan id',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center the pod belongs to',
  `pod_id` BIGINT NOT NULL
  COMMENT 'pod the vlan belongs to',
  `account_id` BIGINT NULL
  COMMENT 'account the vlan belongs to right now',
  `taken` DATETIME NULL
  COMMENT 'Date taken'
);

CREATE TABLE `op_router_monitoring_services` (
  `vm_id` BIGINT NOT NULL
  COMMENT 'Primary Key'
    PRIMARY KEY,
  `router_name` VARCHAR(255) NOT NULL
  COMMENT 'Name of the Virtual Router',
  `last_alert_timestamp` VARCHAR(255) NOT NULL
  COMMENT 'Timestamp of the last alert received from Virtual Router',
  CONSTRAINT `vm_id`
  UNIQUE (`vm_id`)
);

CREATE TABLE `op_user_stats_log` (
  `user_stats_id` BIGINT NOT NULL,
  `net_bytes_received` BIGINT DEFAULT '0' NOT NULL,
  `net_bytes_sent` BIGINT DEFAULT '0' NOT NULL,
  `current_bytes_received` BIGINT DEFAULT '0' NOT NULL,
  `current_bytes_sent` BIGINT DEFAULT '0' NOT NULL,
  `agg_bytes_received` BIGINT DEFAULT '0' NOT NULL,
  `agg_bytes_sent` BIGINT DEFAULT '0' NOT NULL,
  `updated` DATETIME NULL
  COMMENT 'stats update timestamp',
  CONSTRAINT `user_stats_id`
  UNIQUE (`user_stats_id`, `updated`)
);

CREATE TABLE `op_vm_ruleset_log` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `instance_id` BIGINT NOT NULL
  COMMENT 'vm instance that needs rules to be synced.',
  `created` DATETIME NOT NULL
  COMMENT 'time the entry was requested',
  `logsequence` BIGINT NULL
  COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  CONSTRAINT `u_op_vm_ruleset_log__instance_id`
  UNIQUE (`instance_id`)
);

CREATE TABLE `op_vpc_distributed_router_sequence_no` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `vpc_id` BIGINT NOT NULL
  COMMENT 'vpc id.',
  `topology_update_sequence_no` BIGINT NULL
  COMMENT 'sequence number to be sent to hypervisor, uniquely identifies a VPC topology update',
  `routing_policy__update_sequence_no` BIGINT NULL
  COMMENT 'sequence number to be sent to hypervisor, uniquely identifies a routing policy update',
  CONSTRAINT `u_op_vpc_distributed_router_sequence_no_vpc_id`
  UNIQUE (`vpc_id`)
);

CREATE TABLE `ovs_providers` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `nsp_id` BIGINT NOT NULL
  COMMENT 'Network Service Provider ID',
  `uuid` VARCHAR(40) NULL,
  `enabled` INT(1) NOT NULL
  COMMENT 'Enabled or disabled',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  CONSTRAINT `uc_ovs_providers__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_ovs_providers__nsp_id`
  ON `ovs_providers` (`nsp_id`);

CREATE TABLE `ovs_tunnel_interface` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `ip` VARCHAR(16) NULL,
  `netmask` VARCHAR(16) NULL,
  `mac` VARCHAR(18) NULL,
  `host_id` BIGINT NULL,
  `label` VARCHAR(45) NULL
);

CREATE TABLE `ovs_tunnel_network` (
  `id` BIGINT AUTO_INCREMENT,
  `from` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'from host id',
  `to` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'to host id',
  `network_id` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'network identifier',
  `key` INT(10) UNSIGNED NULL
  COMMENT 'gre key',
  `port_name` VARCHAR(32) NULL
  COMMENT 'in port on open vswitch',
  `state` VARCHAR(16) DEFAULT 'FAILED' NULL
  COMMENT 'result of tunnel creatation',
  PRIMARY KEY (`from`, `to`, `network_id`),
  CONSTRAINT `id`
  UNIQUE (`id`)
);

CREATE TABLE `physical_network` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `name` VARCHAR(255) NOT NULL,
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center id that this physical network belongs to',
  `vnet` VARCHAR(255) NULL,
  `speed` VARCHAR(32) NULL,
  `domain_id` BIGINT NULL
  COMMENT 'foreign key to domain id',
  `broadcast_domain_range` VARCHAR(32) DEFAULT 'POD' NOT NULL
  COMMENT 'range of broadcast domain : POD/ZONE',
  `state` VARCHAR(32) DEFAULT 'Disabled' NOT NULL
  COMMENT 'what state is this configuration in',
  `created` DATETIME NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  CONSTRAINT `uc_physical_networks__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_physical_network__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_physical_network__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
);

CREATE INDEX `i_physical_network__data_center_id`
  ON `physical_network` (`data_center_id`);

CREATE INDEX `i_physical_network__domain_id`
  ON `physical_network` (`domain_id`);

CREATE INDEX `i_physical_network__removed`
  ON `physical_network` (`removed`);

ALTER TABLE `account_vnet_map`
  ADD CONSTRAINT `fk_account_vnet_map__physical_network_id`
FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `external_load_balancer_devices`
  ADD CONSTRAINT `fk_external_lb_devices_physical_network_id`
FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `external_nicira_nvp_devices`
  ADD CONSTRAINT `fk_external_nicira_nvp_devices__physical_network_id`
FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `external_nuage_vsp_devices`
  ADD CONSTRAINT `fk_external_nuage_vsp_devices__physical_network_id`
FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `op_dc_vnet_alloc`
  ADD CONSTRAINT `fk_op_dc_vnet_alloc__physical_network_id`
FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `physical_network_isolation_methods` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'id of the physical network',
  `isolation_method` VARCHAR(255) NOT NULL
  COMMENT 'isolation method(VLAN, L3 or GRE)',
  CONSTRAINT `physical_network_id`
  UNIQUE (`physical_network_id`, `isolation_method`),
  CONSTRAINT `fk_physical_network_imethods__physical_network_id`
  FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `physical_network_service_providers` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'id of the physical network',
  `provider_name` VARCHAR(255) NOT NULL
  COMMENT 'Service Provider name',
  `state` VARCHAR(32) DEFAULT 'Disabled' NOT NULL
  COMMENT 'provider state',
  `destination_physical_network_id` BIGINT NULL
  COMMENT 'id of the physical network to bridge to',
  `vpn_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is VPN service provided',
  `dhcp_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is DHCP service provided',
  `dns_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is DNS service provided',
  `gateway_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is Gateway service provided',
  `firewall_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is Firewall service provided',
  `source_nat_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is Source NAT service provided',
  `load_balance_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is LB service provided',
  `static_nat_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is Static NAT service provided',
  `port_forwarding_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is Port Forwarding service provided',
  `user_data_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is UserData service provided',
  `security_group_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is SG service provided',
  `networkacl_service_provided` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is Network ACL service provided',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  CONSTRAINT `uc_service_providers__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_pnetwork_service_providers__physical_network_id`
  FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_pnetwork_service_providers__physical_network_id`
  ON `physical_network_service_providers` (`physical_network_id`);

ALTER TABLE `ovs_providers`
  ADD CONSTRAINT `fk_ovs_providers__nsp_id`
FOREIGN KEY (`nsp_id`) REFERENCES `cloud`.`physical_network_service_providers` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `physical_network_tags` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'id of the physical network',
  `tag` VARCHAR(255) NOT NULL
  COMMENT 'tag',
  CONSTRAINT `physical_network_id`
  UNIQUE (`physical_network_id`, `tag`),
  CONSTRAINT `fk_physical_network_tags__physical_network_id`
  FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `physical_network_traffic_types` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'id of the physical network',
  `traffic_type` VARCHAR(32) NOT NULL
  COMMENT 'type of traffic going through this network',
  `xenserver_network_label` VARCHAR(255) NULL
  COMMENT 'The network name label of the physical device dedicated to this traffic on a XenServer host',
  `kvm_network_label` VARCHAR(255) DEFAULT 'cloudbr0' NULL
  COMMENT 'The network name label of the physical device dedicated to this traffic on a KVM host',
  `ovm_network_label` VARCHAR(255) NULL
  COMMENT 'The network name label of the physical device dedicated to this traffic on a Ovm host',
  `vlan` VARCHAR(255) NULL
  COMMENT 'The vlan tag to be sent down to a VMware host',
  CONSTRAINT `uc_traffic_types__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `physical_network_id`
  UNIQUE (`physical_network_id`, `traffic_type`),
  CONSTRAINT `fk_physical_network_traffic_types__physical_network_id`
  FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `pod_vlan_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `pod_id` BIGINT NOT NULL
  COMMENT 'pod id. foreign key to pod table',
  `vlan_db_id` BIGINT NOT NULL
  COMMENT 'database id of vlan. foreign key to vlan table',
  CONSTRAINT `fk_pod_vlan_map__pod_id`
  FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_pod_vlan_map__pod_id`
  ON `pod_vlan_map` (`pod_id`);

CREATE INDEX `i_pod_vlan_map__vlan_id`
  ON `pod_vlan_map` (`vlan_db_id`);

CREATE TABLE `port_forwarding_rules` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `instance_id` BIGINT NOT NULL
  COMMENT 'vm instance id',
  `dest_ip_address` CHAR(40) NOT NULL
  COMMENT 'id_address',
  `dest_port_start` INT(10) NOT NULL
  COMMENT 'starting port of the port range to map to',
  `dest_port_end` INT(10) NOT NULL
  COMMENT 'end port of the the port range to map to',
  CONSTRAINT `fk_port_forwarding_rules__id`
  FOREIGN KEY (`id`) REFERENCES `cloud`.`firewall_rules` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_port_forwarding_rules__instance_id`
  ON `port_forwarding_rules` (`instance_id`);

CREATE TABLE `portable_ip_address` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `account_id` BIGINT NULL,
  `domain_id` BIGINT NULL,
  `allocated` DATETIME NULL
  COMMENT 'Date portable ip was allocated',
  `state` CHAR(32) DEFAULT 'Free' NOT NULL
  COMMENT 'state of the portable ip address',
  `region_id` INT(10) UNSIGNED NOT NULL,
  `vlan` VARCHAR(255) NULL,
  `gateway` VARCHAR(255) NULL,
  `netmask` VARCHAR(255) NULL,
  `portable_ip_address` VARCHAR(255) NULL,
  `portable_ip_range_id` BIGINT NOT NULL,
  `data_center_id` BIGINT NULL
  COMMENT 'zone to which portable IP is associated',
  `physical_network_id` BIGINT NULL
  COMMENT 'physical network id in the zone to which portable IP is associated',
  `network_id` BIGINT NULL
  COMMENT 'guest network to which portable ip address is associated with',
  `vpc_id` BIGINT NULL
  COMMENT 'vpc to which portable ip address is associated with'
);

CREATE INDEX `i_portable_ip_address__portable_ip_range_id`
  ON `portable_ip_address` (`portable_ip_range_id`);

CREATE INDEX `i_portable_ip_address__region_id`
  ON `portable_ip_address` (`region_id`);

CREATE TABLE `portable_ip_range` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `region_id` INT(10) UNSIGNED NOT NULL,
  `vlan_id` VARCHAR(255) NULL,
  `gateway` VARCHAR(255) NULL,
  `netmask` VARCHAR(255) NULL,
  `start_ip` VARCHAR(255) NULL,
  `end_ip` VARCHAR(255) NULL
);

CREATE INDEX `i_portableip__region_id`
  ON `portable_ip_range` (`region_id`);

ALTER TABLE `portable_ip_address`
  ADD CONSTRAINT `fk_portable_ip_address__portable_ip_range_id`
FOREIGN KEY (`portable_ip_range_id`) REFERENCES `cloud`.`portable_ip_range` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `private_ip_address` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'primary key'
    PRIMARY KEY,
  `ip_address` CHAR(40) NOT NULL
  COMMENT 'ip address',
  `network_id` BIGINT NOT NULL
  COMMENT 'id of the network ip belongs to',
  `reservation_id` CHAR(40) NULL
  COMMENT 'reservation id',
  `mac_address` VARCHAR(17) NULL
  COMMENT 'mac address',
  `vpc_id` BIGINT NULL
  COMMENT 'vpc this ip belongs to',
  `taken` DATETIME NULL
  COMMENT 'Date taken',
  `source_nat` TINYINT(1) DEFAULT '0' NULL,
  CONSTRAINT `fk_private_ip_address__network_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_private_ip_address__network_id`
  ON `private_ip_address` (`network_id`);

CREATE INDEX `i_private_ip_address__vpc_id`
  ON `private_ip_address` (`vpc_id`);

CREATE TABLE `project_account` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `account_id` BIGINT NOT NULL
  COMMENT 'account id',
  `account_role` VARCHAR(255) DEFAULT 'Regular' NOT NULL
  COMMENT 'Account role in the project (Owner or Regular)',
  `project_id` BIGINT NOT NULL
  COMMENT 'project id',
  `project_account_id` BIGINT NOT NULL,
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `account_id`
  UNIQUE (`account_id`, `project_id`),
  CONSTRAINT `fk_project_account__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_project_account__project_account_id`
  FOREIGN KEY (`project_account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_project_account__project_account_id`
  ON `project_account` (`project_account_id`);

CREATE INDEX `i_project_account__project_id`
  ON `project_account` (`project_id`);

CREATE TABLE `project_invitations` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `project_id` BIGINT NOT NULL
  COMMENT 'project id',
  `account_id` BIGINT NULL
  COMMENT 'account id',
  `domain_id` BIGINT NULL
  COMMENT 'domain id',
  `email` VARCHAR(255) NULL
  COMMENT 'email',
  `token` VARCHAR(255) NULL
  COMMENT 'token',
  `state` VARCHAR(255) DEFAULT 'Pending' NOT NULL
  COMMENT 'the state of the invitation',
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `uc_project_invitations__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `project_id`
  UNIQUE (`project_id`, `account_id`),
  CONSTRAINT `project_id_2`
  UNIQUE (`project_id`, `email`),
  CONSTRAINT `project_id_3`
  UNIQUE (`project_id`, `token`),
  CONSTRAINT `fk_project_invitations__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_project_invitations__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_project_invitations__account_id`
  ON `project_invitations` (`account_id`);

CREATE INDEX `i_project_invitations__domain_id`
  ON `project_invitations` (`domain_id`);

CREATE TABLE `projects` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NULL
  COMMENT 'project name',
  `uuid` VARCHAR(40) NULL,
  `display_text` VARCHAR(255) NULL
  COMMENT 'project name',
  `project_account_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `created` DATETIME NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `state` VARCHAR(255) NOT NULL
  COMMENT 'state of the project (Active/Inactive/Suspended)',
  CONSTRAINT `uc_projects__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_projects__project_account_id`
  FOREIGN KEY (`project_account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_projects__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_projects__domain_id`
  ON `projects` (`domain_id`);

CREATE INDEX `i_projects__project_account_id`
  ON `projects` (`project_account_id`);

CREATE INDEX `i_projects__removed`
  ON `projects` (`removed`);

ALTER TABLE `project_account`
  ADD CONSTRAINT `fk_project_account__project_id`
FOREIGN KEY (`project_id`) REFERENCES `cloud`.`projects` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `project_invitations`
  ADD CONSTRAINT `fk_project_invitations__project_id`
FOREIGN KEY (`project_id`) REFERENCES `cloud`.`projects` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `region` (
  `id` INT(10) UNSIGNED NOT NULL
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `end_point` VARCHAR(255) NOT NULL,
  `portableip_service_enabled` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is Portable IP service enalbed in the Region',
  `gslb_service_enabled` TINYINT(1) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'Is GSLB service enalbed in the Region',
  CONSTRAINT `id_2`
  UNIQUE (`id`),
  CONSTRAINT `id_3`
  UNIQUE (`id`),
  CONSTRAINT `name`
  UNIQUE (`name`)
);

ALTER TABLE `global_load_balancing_rules`
  ADD CONSTRAINT `fk_global_load_balancing_rules_region_id`
FOREIGN KEY (`region_id`) REFERENCES `cloud`.`region` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `portable_ip_address`
  ADD CONSTRAINT `fk_portable_ip_address__region_id`
FOREIGN KEY (`region_id`) REFERENCES `cloud`.`region` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `portable_ip_range`
  ADD CONSTRAINT `fk_portableip__region_id`
FOREIGN KEY (`region_id`) REFERENCES `cloud`.`region` (`id`);

CREATE TABLE `remote_access_vpn` (
  `vpn_server_addr_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `network_id` BIGINT NULL,
  `domain_id` BIGINT NOT NULL,
  `local_ip` CHAR(40) NOT NULL,
  `ip_range` VARCHAR(32) NOT NULL,
  `ipsec_psk` VARCHAR(256) NOT NULL,
  `state` CHAR(32) NOT NULL,
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `vpc_id` BIGINT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the entry can be displayed to the end user',
  CONSTRAINT `vpn_server_addr_id`
  UNIQUE (`vpn_server_addr_id`),
  CONSTRAINT `uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_remote_access_vpn__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__network_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_remote_access_vpn__account_id`
  ON `remote_access_vpn` (`account_id`);

CREATE INDEX `i_remote_access_vpn__domain_id`
  ON `remote_access_vpn` (`domain_id`);

CREATE INDEX `i_remote_access_vpn__network_id`
  ON `remote_access_vpn` (`network_id`);

CREATE TABLE `remote_access_vpn_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `remote_access_vpn_id` BIGINT NOT NULL
  COMMENT 'Remote access vpn id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_remote_access_vpn_details__remote_access_vpn_id`
  FOREIGN KEY (`remote_access_vpn_id`) REFERENCES `cloud`.`remote_access_vpn` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_remote_access_vpn_details__remote_access_vpn_id`
  ON `remote_access_vpn_details` (`remote_access_vpn_id`);

CREATE TABLE `resource_count` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `account_id` BIGINT NULL,
  `domain_id` BIGINT NULL,
  `type` VARCHAR(255) NULL,
  `count` BIGINT DEFAULT '0' NOT NULL,
  CONSTRAINT `i_resource_count__type_accountId`
  UNIQUE (`type`, `account_id`),
  CONSTRAINT `i_resource_count__type_domaintId`
  UNIQUE (`type`, `domain_id`),
  CONSTRAINT `fk_resource_count__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_resource_count__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_resource_count__account_id`
  ON `resource_count` (`account_id`);

CREATE INDEX `i_resource_count__domain_id`
  ON `resource_count` (`domain_id`);

CREATE INDEX `i_resource_count__type`
  ON `resource_count` (`type`);

CREATE TABLE `resource_limit` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `domain_id` BIGINT NULL,
  `account_id` BIGINT NULL,
  `type` VARCHAR(255) NULL,
  `max` BIGINT DEFAULT '-1' NOT NULL,
  CONSTRAINT `fk_resource_limit__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_resource_limit__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_resource_limit__account_id`
  ON `resource_limit` (`account_id`);

CREATE INDEX `i_resource_limit__domain_id`
  ON `resource_limit` (`domain_id`);

CREATE TABLE `resource_tags` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `key` VARCHAR(255) NULL,
  `value` VARCHAR(255) NULL,
  `resource_id` BIGINT NOT NULL,
  `resource_uuid` VARCHAR(40) NULL,
  `resource_type` VARCHAR(255) NULL,
  `customer` VARCHAR(255) NULL,
  `domain_id` BIGINT NOT NULL
  COMMENT 'foreign key to domain id',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner of this network',
  CONSTRAINT `uc_resource_tags__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `i_tags__resource_id__resource_type__key`
  UNIQUE (`resource_id`, `resource_type`, `key`),
  CONSTRAINT `fk_tags__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`),
  CONSTRAINT `fk_tags__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
);

CREATE INDEX `i_tags__account_id`
  ON `resource_tags` (`account_id`);

CREATE INDEX `i_tags__domain_id`
  ON `resource_tags` (`domain_id`);

CREATE TABLE `router_network_ref` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `router_id` BIGINT NOT NULL
  COMMENT 'router id',
  `network_id` BIGINT NOT NULL
  COMMENT 'network id',
  `guest_type` CHAR(32) NULL
  COMMENT 'type of guest network that can be shared or isolated',
  CONSTRAINT `i_router_network_ref__router_id__network_id`
  UNIQUE (`router_id`, `network_id`),
  CONSTRAINT `fk_router_network_ref__router_id`
  FOREIGN KEY (`router_id`) REFERENCES `cloud`.`domain_router` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_router_network_ref__networks_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_router_network_ref__networks_id`
  ON `router_network_ref` (`network_id`);

CREATE TABLE `s2s_customer_gateway` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `name` VARCHAR(255) NOT NULL,
  `gateway_ip` CHAR(40) NOT NULL,
  `guest_cidr_list` VARCHAR(4096) NULL,
  `ipsec_psk` VARCHAR(256) NULL,
  `ike_policy` VARCHAR(30) NOT NULL,
  `esp_policy` VARCHAR(30) NOT NULL,
  `ike_lifetime` INT DEFAULT '86400' NOT NULL,
  `esp_lifetime` INT DEFAULT '3600' NOT NULL,
  `dpd` INT(1) DEFAULT '0' NOT NULL,
  `force_encap` INT(1) DEFAULT '0' NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  CONSTRAINT `uc_s2s_customer_gateway__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_s2s_customer_gateway__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_s2s_customer_gateway__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_s2s_customer_gateway__account_id`
  ON `s2s_customer_gateway` (`account_id`);

CREATE INDEX `i_s2s_customer_gateway__domain_id`
  ON `s2s_customer_gateway` (`domain_id`);

CREATE TABLE `s2s_customer_gateway_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `s2s_customer_gateway_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_s2s_customer_gateway_details__s2s_customer_gateway_id`
  FOREIGN KEY (`s2s_customer_gateway_id`) REFERENCES `cloud`.`s2s_customer_gateway` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_s2s_customer_gateway_details__s2s_customer_gateway_id`
  ON `s2s_customer_gateway_details` (`s2s_customer_gateway_id`);

CREATE TABLE `s2s_vpn_connection` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `vpn_gateway_id` BIGINT NULL,
  `customer_gateway_id` BIGINT NULL,
  `state` VARCHAR(32) NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `passive` INT(1) UNSIGNED DEFAULT '0' NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the entry can be displayed to the end user',
  CONSTRAINT `uc_s2s_vpn_connection__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_s2s_vpn_connection__customer_gateway_id`
  FOREIGN KEY (`customer_gateway_id`) REFERENCES `cloud`.`s2s_customer_gateway` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_s2s_vpn_connection__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_s2s_vpn_connection__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_s2s_vpn_connection__account_id`
  ON `s2s_vpn_connection` (`account_id`);

CREATE INDEX `i_s2s_vpn_connection__customer_gateway_id`
  ON `s2s_vpn_connection` (`customer_gateway_id`);

CREATE INDEX `i_s2s_vpn_connection__domain_id`
  ON `s2s_vpn_connection` (`domain_id`);

CREATE INDEX `i_s2s_vpn_connection__vpn_gateway_id`
  ON `s2s_vpn_connection` (`vpn_gateway_id`);

CREATE TABLE `s2s_vpn_connection_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `s2s_vpn_connection_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_s2s_vpn_connection_details__s2s_vpn_connection_id`
  FOREIGN KEY (`s2s_vpn_connection_id`) REFERENCES `cloud`.`s2s_vpn_connection` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_s2s_vpn_connection_details__s2s_vpn_connection_id`
  ON `s2s_vpn_connection_details` (`s2s_vpn_connection_id`);

CREATE TABLE `s2s_vpn_gateway` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `addr_id` BIGINT NOT NULL,
  `vpc_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the entry can be displayed to the end user',
  CONSTRAINT `uc_s2s_vpn_gateway__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_s2s_vpn_gateway__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_s2s_vpn_gateway__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_s2s_vpn_gateway__account_id`
  ON `s2s_vpn_gateway` (`account_id`);

CREATE INDEX `i_s2s_vpn_gateway__addr_id`
  ON `s2s_vpn_gateway` (`addr_id`);

CREATE INDEX `i_s2s_vpn_gateway__domain_id`
  ON `s2s_vpn_gateway` (`domain_id`);

CREATE INDEX `i_s2s_vpn_gateway__vpc_id`
  ON `s2s_vpn_gateway` (`vpc_id`);

ALTER TABLE `s2s_vpn_connection`
  ADD CONSTRAINT `fk_s2s_vpn_connection__vpn_gateway_id`
FOREIGN KEY (`vpn_gateway_id`) REFERENCES `cloud`.`s2s_vpn_gateway` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `s2s_vpn_gateway_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `s2s_vpn_gateway_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_s2s_vpn_gateway_details__s2s_vpn_gateway_id`
  FOREIGN KEY (`s2s_vpn_gateway_id`) REFERENCES `cloud`.`s2s_vpn_gateway` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_s2s_vpn_gateway_details__s2s_vpn_gateway_id`
  ON `s2s_vpn_gateway_details` (`s2s_vpn_gateway_id`);

CREATE TABLE `saml_token` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(255) NOT NULL
  COMMENT 'The Authn Unique Id',
  `domain_id` BIGINT NULL,
  `entity` TEXT NOT NULL
  COMMENT 'Identity Provider Entity Id',
  `created` DATETIME NOT NULL,
  CONSTRAINT `uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_saml_token__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_saml_token__domain_id`
  ON `saml_token` (`domain_id`);

CREATE TABLE `secondary_storage_vm` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `public_mac_address` VARCHAR(17) NULL
  COMMENT 'mac address of the public facing network card',
  `public_ip_address` CHAR(40) NULL
  COMMENT 'public ip address for the sec storage vm',
  `public_netmask` VARCHAR(15) NULL
  COMMENT 'public netmask used for the sec storage vm',
  `guid` VARCHAR(255) NULL
  COMMENT 'copied from guid of secondary storage host',
  `nfs_share` VARCHAR(255) NULL
  COMMENT 'server and path exported by the nfs server',
  `last_update` DATETIME NULL
  COMMENT 'Last session update time',
  `role` VARCHAR(64) DEFAULT 'templateProcessor' NOT NULL
  COMMENT 'work role of secondary storage host(templateProcessor | commandExecutor)',
  CONSTRAINT `public_mac_address`
  UNIQUE (`public_mac_address`)
);

CREATE TABLE `security_group` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL,
  `description` VARCHAR(4096) NULL,
  `domain_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  CONSTRAINT `name`
  UNIQUE (`name`, `account_id`),
  CONSTRAINT `uc_security_group__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_security_group__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`),
  CONSTRAINT `fk_security_group__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_security_group__account_id`
  ON `security_group` (`account_id`);

CREATE INDEX `i_security_group__domain_id`
  ON `security_group` (`domain_id`);

CREATE INDEX `i_security_group_name`
  ON `security_group` (`name`);

CREATE TABLE `security_group_rule` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `security_group_id` BIGINT NOT NULL,
  `type` VARCHAR(10) DEFAULT 'ingress' NULL,
  `start_port` VARCHAR(10) NULL,
  `end_port` VARCHAR(10) NULL,
  `protocol` VARCHAR(16) DEFAULT 'TCP' NOT NULL,
  `allowed_network_id` BIGINT NULL,
  `allowed_ip_cidr` VARCHAR(44) NULL,
  `create_status` VARCHAR(32) NULL
  COMMENT 'rule creation status',
  CONSTRAINT `uc_security_group_rule__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_security_group_rule___security_group_id`
  FOREIGN KEY (`security_group_id`) REFERENCES `cloud`.`security_group` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_security_group_rule___allowed_network_id`
  FOREIGN KEY (`allowed_network_id`) REFERENCES `cloud`.`security_group` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_security_group_rule_allowed_network`
  ON `security_group_rule` (`allowed_network_id`);

CREATE INDEX `i_security_group_rule_network_id`
  ON `security_group_rule` (`security_group_id`);

CREATE TABLE `security_group_vm_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `security_group_id` BIGINT NOT NULL,
  `instance_id` BIGINT NOT NULL,
  CONSTRAINT `fk_security_group_vm_map___security_group_id`
  FOREIGN KEY (`security_group_id`) REFERENCES `cloud`.`security_group` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_security_group_vm_map___instance_id`
  ON `security_group_vm_map` (`instance_id`);

CREATE INDEX `i_security_group_vm_map___security_group_id`
  ON `security_group_vm_map` (`security_group_id`);

CREATE TABLE `sequence` (
  `name` VARCHAR(64) NOT NULL
  COMMENT 'name of the sequence'
    PRIMARY KEY,
  `value` BIGINT NOT NULL
  COMMENT 'sequence value',
  CONSTRAINT `name`
  UNIQUE (`name`)
);

CREATE TABLE `service_offering` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `cpu` INT(10) UNSIGNED NULL
  COMMENT '# of cores',
  `speed` INT(10) UNSIGNED NULL
  COMMENT 'speed per core in mhz',
  `ram_size` BIGINT NULL,
  `nw_rate` SMALLINT DEFAULT '200' NULL
  COMMENT 'network rate throttle mbits/s',
  `mc_rate` SMALLINT DEFAULT '10' NULL
  COMMENT 'mcast rate throttle mbits/s',
  `ha_enabled` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Enable HA',
  `limit_cpu_use` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Limit the CPU usage to service offering',
  `host_tag` VARCHAR(255) NULL
  COMMENT 'host tag specified by the service_offering',
  `default_use` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'is this offering a default system offering',
  `vm_type` VARCHAR(32) NULL
  COMMENT 'type of offering specified for system offerings',
  `sort_key` INT(32) DEFAULT '0' NOT NULL
  COMMENT 'sort key used for customising sort method',
  `is_volatile` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if the vm needs to be volatile, i.e., on every reboot of vm from API root disk is discarded and creates a new root disk',
  `deployment_planner` VARCHAR(255) NULL
  COMMENT 'Planner heuristics used to deploy a VM of this offering; if null global config vm.deployment.planner is used',
  CONSTRAINT `fk_service_offering__id`
  FOREIGN KEY (`id`) REFERENCES `cloud`.`disk_offering` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `service_offering_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `service_offering_id` BIGINT NOT NULL
  COMMENT 'service offering id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `uk_service_offering_id_name`
  UNIQUE (`service_offering_id`, `name`),
  CONSTRAINT `fk_service_offering_details__service_offering_id`
  FOREIGN KEY (`service_offering_id`) REFERENCES `cloud`.`service_offering` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `snapshot_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `snapshot_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE TABLE `snapshot_policy` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `volume_id` BIGINT NOT NULL,
  `schedule` VARCHAR(100) NOT NULL
  COMMENT 'schedule time of execution',
  `timezone` VARCHAR(100) NOT NULL
  COMMENT 'the timezone in which the schedule time is specified',
  `interval` INT(4) DEFAULT '4' NOT NULL
  COMMENT 'backup schedule, e.g. hourly, daily, etc.',
  `max_snaps` INT(8) DEFAULT '0' NOT NULL
  COMMENT 'maximum number of snapshots to maintain',
  `active` TINYINT(1) UNSIGNED NOT NULL
  COMMENT 'Is the policy active',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the policy can be displayed to the end user',
  CONSTRAINT `uc_snapshot_policy__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_snapshot_policy__volume_id`
  ON `snapshot_policy` (`volume_id`);

CREATE TABLE `snapshot_policy_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `policy_id` BIGINT NOT NULL
  COMMENT 'snapshot policy id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_snapshot_policy_details__snapshot_policy_id`
  FOREIGN KEY (`policy_id`) REFERENCES `cloud`.`snapshot_policy` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_snapshot_policy_details__snapshot_policy_id`
  ON `snapshot_policy_details` (`policy_id`);

CREATE TABLE `snapshot_schedule` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `volume_id` BIGINT NOT NULL
  COMMENT 'The volume for which this snapshot is being taken',
  `policy_id` BIGINT NOT NULL
  COMMENT 'One of the policyIds for which this snapshot was taken',
  `scheduled_timestamp` DATETIME NOT NULL
  COMMENT 'Time at which the snapshot was scheduled for execution',
  `async_job_id` BIGINT NULL
  COMMENT 'If this schedule is being executed, it is the id of the create aysnc_job. Before that it is null',
  `snapshot_id` BIGINT NULL
  COMMENT 'If this schedule is being executed, then the corresponding snapshot has this id. Before that it is null',
  CONSTRAINT `uc_snapshot_schedule__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `volume_id`
  UNIQUE (`volume_id`, `policy_id`),
  CONSTRAINT `fk__snapshot_schedule_policy_id`
  FOREIGN KEY (`policy_id`) REFERENCES `cloud`.`snapshot_policy` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_async_job_id`
  FOREIGN KEY (`async_job_id`) REFERENCES `cloud`.`async_job` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_snapshot_schedule__async_job_id`
  ON `snapshot_schedule` (`async_job_id`);

CREATE INDEX `i_snapshot_schedule__policy_id`
  ON `snapshot_schedule` (`policy_id`);

CREATE INDEX `i_snapshot_schedule__scheduled_timestamp`
  ON `snapshot_schedule` (`scheduled_timestamp`);

CREATE INDEX `i_snapshot_schedule__snapshot_id`
  ON `snapshot_schedule` (`snapshot_id`);

CREATE INDEX `i_snapshot_schedule__volume_id`
  ON `snapshot_schedule` (`volume_id`);

CREATE TABLE `snapshot_store_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `store_id` BIGINT NOT NULL,
  `snapshot_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `store_role` VARCHAR(255) NULL,
  `size` BIGINT NULL,
  `physical_size` BIGINT DEFAULT '0' NULL,
  `parent_snapshot_id` BIGINT DEFAULT '0' NULL,
  `install_path` VARCHAR(255) NULL,
  `state` VARCHAR(255) NOT NULL,
  `update_count` BIGINT NULL,
  `ref_cnt` BIGINT NULL,
  `updated` DATETIME NULL,
  `volume_id` BIGINT NULL
);

CREATE INDEX `i_snapshot_store_ref__snapshot_id`
  ON `snapshot_store_ref` (`snapshot_id`);

CREATE INDEX `i_snapshot_store_ref__store_id`
  ON `snapshot_store_ref` (`store_id`);

CREATE TABLE `snapshots` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'Primary Key'
    PRIMARY KEY,
  `data_center_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL
  COMMENT 'owner.  foreign key to account table',
  `domain_id` BIGINT NOT NULL
  COMMENT 'the domain that the owner belongs to',
  `volume_id` BIGINT NOT NULL
  COMMENT 'volume it belongs to. foreign key to volume table',
  `disk_offering_id` BIGINT NOT NULL
  COMMENT 'from original volume',
  `status` VARCHAR(32) NULL
  COMMENT 'snapshot creation status',
  `path` VARCHAR(255) NULL
  COMMENT 'Path',
  `name` VARCHAR(255) NOT NULL
  COMMENT 'snapshot name',
  `uuid` VARCHAR(40) NULL,
  `snapshot_type` INT(4) NOT NULL
  COMMENT 'type of snapshot, e.g. manual, recurring',
  `type_description` VARCHAR(25) NULL
  COMMENT 'description of the type of snapshot, e.g. manual, recurring',
  `size` BIGINT NOT NULL
  COMMENT 'original disk size of snapshot',
  `created` DATETIME NULL
  COMMENT 'Date Created',
  `removed` DATETIME NULL
  COMMENT 'Date removed.  not null if removed',
  `backup_snap_id` VARCHAR(255) NULL
  COMMENT 'Back up uuid of the snapshot',
  `sechost_id` BIGINT NULL
  COMMENT 'secondary storage host id',
  `prev_snap_id` BIGINT NULL
  COMMENT 'Id of the most recent snapshot',
  `hypervisor_type` VARCHAR(32) NOT NULL
  COMMENT 'hypervisor that the snapshot was taken under',
  `version` VARCHAR(32) NULL
  COMMENT 'snapshot version',
  `min_iops` BIGINT NULL
  COMMENT 'Minimum IOPS',
  `max_iops` BIGINT NULL
  COMMENT 'Maximum IOPS',
  CONSTRAINT `uc_snapshots__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_snapshots__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
);

CREATE INDEX `i_snapshots__account_id`
  ON `snapshots` (`account_id`);

CREATE INDEX `i_snapshots__name`
  ON `snapshots` (`name`);

CREATE INDEX `i_snapshots__prev_snap_id`
  ON `snapshots` (`prev_snap_id`);

CREATE INDEX `i_snapshots__removed`
  ON `snapshots` (`removed`);

CREATE INDEX `i_snapshots__snapshot_type`
  ON `snapshots` (`snapshot_type`);

CREATE INDEX `i_snapshots__volume_id`
  ON `snapshots` (`volume_id`);

ALTER TABLE `snapshot_schedule`
  ADD CONSTRAINT `fk__snapshot_schedule_snapshot_id`
FOREIGN KEY (`snapshot_id`) REFERENCES `cloud`.`snapshots` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `snapshot_store_ref`
  ADD CONSTRAINT `fk_snapshot_store_ref__snapshot_id`
FOREIGN KEY (`snapshot_id`) REFERENCES `cloud`.`snapshots` (`id`);

CREATE TABLE `ssh_keypairs` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `account_id` BIGINT NOT NULL
  COMMENT 'owner, foreign key to account table',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain, foreign key to domain table',
  `keypair_name` VARCHAR(256) NOT NULL
  COMMENT 'name of the key pair',
  `fingerprint` VARCHAR(128) NOT NULL
  COMMENT 'fingerprint for the ssh public key',
  `public_key` VARCHAR(5120) NOT NULL
  COMMENT 'public key of the ssh key pair',
  CONSTRAINT `unique_index`
  UNIQUE (`fingerprint`, `account_id`),
  CONSTRAINT `fk_ssh_keypairs__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_ssh_keypairs__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_ssh_keypairs__account_id`
  ON `ssh_keypairs` (`account_id`);

CREATE INDEX `i_ssh_keypairs__domain_id`
  ON `ssh_keypairs` (`domain_id`);

CREATE INDEX `i_public_key`
  ON `ssh_keypairs` (`public_key`);

CREATE TABLE `sslcerts` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `account_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `certificate` TEXT NOT NULL,
  `fingerprint` VARCHAR(62) NOT NULL,
  `key` TEXT NOT NULL,
  `chain` TEXT NULL,
  `password` VARCHAR(255) NULL,
  CONSTRAINT `fk_sslcert__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_sslcert__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_sslcert__account_id`
  ON `sslcerts` (`account_id`);

CREATE INDEX `i_sslcert__domain_id`
  ON `sslcerts` (`domain_id`);

ALTER TABLE `load_balancer_cert_map`
  ADD CONSTRAINT `fk_load_balancer_cert_map__certificate_id`
FOREIGN KEY (`certificate_id`) REFERENCES `cloud`.`sslcerts` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `stack_maid` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `msid` BIGINT NOT NULL,
  `thread_id` BIGINT NOT NULL,
  `seq` INT(10) UNSIGNED NOT NULL,
  `cleanup_delegate` VARCHAR(128) NULL,
  `cleanup_context` TEXT NULL,
  `created` DATETIME NULL
);

CREATE INDEX `i_stack_maid_created`
  ON `stack_maid` (`created`);

CREATE INDEX `i_stack_maid_msid_thread_id`
  ON `stack_maid` (`msid`, `thread_id`);

CREATE INDEX `i_stack_maid_seq`
  ON `stack_maid` (`msid`, `seq`);

CREATE TABLE `static_routes` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `cidr` VARCHAR(18) NULL
  COMMENT 'cidr for the static route',
  `gateway_ip_address` VARCHAR(45) NULL
  COMMENT 'gateway ip address for the static route',
  `metric` INT(10) DEFAULT '100' NULL
  COMMENT 'metric value for the route',
  `state` CHAR(32) NOT NULL
  COMMENT 'current state of this rule',
  `vpc_id` BIGINT NULL
  COMMENT 'vpc the firewall rule is associated with',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner id',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `created` DATETIME NULL
  COMMENT 'Date created',
  CONSTRAINT `uc_static_routes__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_static_routes__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_static_routes__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_static_routes__account_id`
  ON `static_routes` (`account_id`);

CREATE INDEX `i_static_routes__domain_id`
  ON `static_routes` (`domain_id`);

CREATE INDEX `i_static_routes__vpc_id`
  ON `static_routes` (`vpc_id`);

CREATE TABLE `static_routes_pre510` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `vpc_gateway_id` BIGINT NULL
  COMMENT 'id of the corresponding ip address',
  `cidr` VARCHAR(18) NULL
  COMMENT 'cidr for the static route',
  `state` CHAR(32) NOT NULL
  COMMENT 'current state of this rule',
  `vpc_id` BIGINT NULL
  COMMENT 'vpc the firewall rule is associated with',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner id',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `created` DATETIME NULL
  COMMENT 'Date created',
  CONSTRAINT `uc_static_routes__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_static_routes__account_id`
  ON `static_routes_pre510` (`account_id`);

CREATE INDEX `i_static_routes__domain_id`
  ON `static_routes_pre510` (`domain_id`);

CREATE INDEX `i_static_routes__vpc_gateway_id`
  ON `static_routes_pre510` (`vpc_gateway_id`);

CREATE INDEX `i_static_routes__vpc_id`
  ON `static_routes_pre510` (`vpc_id`);

CREATE TABLE `storage_pool` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NULL
  COMMENT 'should be NOT NULL',
  `uuid` VARCHAR(255) NULL,
  `pool_type` VARCHAR(32) NOT NULL,
  `port` INT(10) UNSIGNED NOT NULL,
  `data_center_id` BIGINT NOT NULL,
  `pod_id` BIGINT NULL,
  `cluster_id` BIGINT NULL
  COMMENT 'foreign key to cluster',
  `used_bytes` BIGINT NULL,
  `capacity_bytes` BIGINT NULL,
  `host_address` VARCHAR(255) NOT NULL
  COMMENT 'FQDN or IP of storage server',
  `user_info` VARCHAR(255) NULL
  COMMENT 'Authorization information for the storage pool. Used by network filesystems',
  `path` VARCHAR(255) NOT NULL
  COMMENT 'Filesystem path that is shared',
  `created` DATETIME NULL
  COMMENT 'date the pool created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `update_time` DATETIME NULL,
  `status` VARCHAR(32) NULL,
  `storage_provider_name` VARCHAR(255) NULL,
  `scope` VARCHAR(255) NULL,
  `hypervisor` VARCHAR(32) NULL,
  `managed` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Should CloudStack manage this storage',
  `capacity_iops` BIGINT NULL
  COMMENT 'IOPS CloudStack can provision from this storage pool',
  CONSTRAINT `uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_storage_pool__pod_id`
  FOREIGN KEY (`pod_id`) REFERENCES `cloud`.`host_pod_ref` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_storage_pool__cluster_id`
  FOREIGN KEY (`cluster_id`) REFERENCES `cloud`.`cluster` (`id`)
);

CREATE INDEX `i_storage_pool__cluster_id`
  ON `storage_pool` (`cluster_id`);

CREATE INDEX `i_storage_pool__pod_id`
  ON `storage_pool` (`pod_id`);

CREATE INDEX `i_storage_pool__removed`
  ON `storage_pool` (`removed`);

CREATE TABLE `storage_pool_details` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `pool_id` BIGINT NOT NULL
  COMMENT 'pool the detail is related to',
  `name` VARCHAR(255) NOT NULL
  COMMENT 'name of the detail',
  `value` VARCHAR(255) NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_storage_pool_details__pool_id`
  FOREIGN KEY (`pool_id`) REFERENCES `cloud`.`storage_pool` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_storage_pool_details__pool_id`
  ON `storage_pool_details` (`pool_id`);

CREATE INDEX `i_storage_pool_details__name__value`
  ON `storage_pool_details` (`name`, `value`);

CREATE TABLE `storage_pool_host_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `host_id` BIGINT NOT NULL,
  `pool_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `local_path` VARCHAR(255) NULL,
  CONSTRAINT `fk_storage_pool_host_ref__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_storage_pool_host_ref__pool_id`
  FOREIGN KEY (`pool_id`) REFERENCES `cloud`.`storage_pool` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_storage_pool_host_ref__host_id`
  ON `storage_pool_host_ref` (`host_id`);

CREATE INDEX `i_storage_pool_host_ref__pool_id`
  ON `storage_pool_host_ref` (`pool_id`);

CREATE TABLE `storage_pool_work` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `pool_id` BIGINT NOT NULL
  COMMENT 'storage pool associated with the vm',
  `vm_id` BIGINT NOT NULL
  COMMENT 'vm identifier',
  `stopped_for_maintenance` TINYINT(3) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'this flag denoted whether the vm was stopped during maintenance',
  `started_after_maintenance` TINYINT(3) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'this flag denoted whether the vm was started after maintenance',
  `mgmt_server_id` BIGINT NOT NULL
  COMMENT 'management server id',
  CONSTRAINT `pool_id`
  UNIQUE (`pool_id`, `vm_id`)
);

CREATE TABLE `sync_queue` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `sync_objtype` VARCHAR(64) NOT NULL,
  `sync_objid` BIGINT NOT NULL,
  `queue_proc_number` BIGINT NULL
  COMMENT 'process number, increase 1 for each iteration',
  `created` DATETIME NULL
  COMMENT 'date created',
  `last_updated` DATETIME NULL
  COMMENT 'date created',
  `queue_size` SMALLINT(6) DEFAULT '0' NOT NULL
  COMMENT 'number of items being processed by the queue',
  `queue_size_limit` SMALLINT(6) DEFAULT '1' NOT NULL
  COMMENT 'max number of items the queue can process concurrently',
  CONSTRAINT `i_sync_queue__objtype__objid`
  UNIQUE (`sync_objtype`, `sync_objid`)
);

CREATE INDEX `i_sync_queue__created`
  ON `sync_queue` (`created`);

CREATE INDEX `i_sync_queue__last_updated`
  ON `sync_queue` (`last_updated`);

CREATE TABLE `sync_queue_item` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `queue_id` BIGINT NOT NULL,
  `content_type` VARCHAR(64) NULL,
  `content_id` BIGINT NULL,
  `queue_proc_msid` BIGINT NULL
  COMMENT 'owner msid when the queue item is being processed',
  `queue_proc_number` BIGINT NULL
  COMMENT 'used to distinguish raw items and items being in process',
  `queue_proc_time` DATETIME NULL
  COMMENT 'when processing started for the item',
  `created` DATETIME NULL
  COMMENT 'time created',
  CONSTRAINT `fk_sync_queue_item__queue_id`
  FOREIGN KEY (`queue_id`) REFERENCES `cloud`.`sync_queue` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_sync_queue_item__created`
  ON `sync_queue_item` (`created`);

CREATE INDEX `i_sync_queue_item__queue_id`
  ON `sync_queue_item` (`queue_id`);

CREATE INDEX `i_sync_queue_item__queue_proc_msid`
  ON `sync_queue_item` (`queue_proc_msid`);

CREATE INDEX `i_sync_queue_item__queue_proc_number`
  ON `sync_queue_item` (`queue_proc_number`);

CREATE INDEX `i_sync_queue__queue_proc_time`
  ON `sync_queue_item` (`queue_proc_time`);

CREATE TABLE `template_host_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `host_id` BIGINT NOT NULL,
  `template_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `download_pct` INT(10) UNSIGNED NULL,
  `size` BIGINT NULL,
  `physical_size` BIGINT DEFAULT '0' NULL,
  `download_state` VARCHAR(255) NULL,
  `error_str` VARCHAR(255) NULL,
  `local_path` VARCHAR(255) NULL,
  `install_path` VARCHAR(255) NULL,
  `url` VARCHAR(255) NULL,
  `destroyed` TINYINT(1) NULL
  COMMENT 'indicates whether the template_host entry was destroyed by the user or not',
  `is_copy` TINYINT(1) DEFAULT '0' NOT NULL
  COMMENT 'indicates whether this was copied',
  CONSTRAINT `fk_template_host_ref__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_template_host_ref__host_id`
  ON `template_host_ref` (`host_id`);

CREATE INDEX `i_template_host_ref__template_id`
  ON `template_host_ref` (`template_id`);

CREATE TABLE `template_spool_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `pool_id` BIGINT NOT NULL,
  `template_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `download_pct` INT(10) UNSIGNED NULL,
  `download_state` VARCHAR(255) NULL,
  `error_str` VARCHAR(255) NULL,
  `local_path` VARCHAR(255) NULL,
  `install_path` VARCHAR(255) NULL,
  `template_size` BIGINT NOT NULL
  COMMENT 'the size of the template on the pool',
  `marked_for_gc` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'if true, the garbage collector will evict the template from this pool.',
  `state` VARCHAR(255) NULL,
  `update_count` BIGINT NULL,
  `updated` DATETIME NULL,
  CONSTRAINT `i_template_spool_ref__template_id__pool_id`
  UNIQUE (`template_id`, `pool_id`),
  CONSTRAINT `fk_template_spool_ref__pool_id`
  FOREIGN KEY (`pool_id`) REFERENCES `cloud`.`storage_pool` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_template_spool_ref__pool_id`
  ON `template_spool_ref` (`pool_id`);

CREATE TABLE `template_store_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `store_id` BIGINT NOT NULL,
  `template_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `download_pct` INT(10) UNSIGNED NULL,
  `size` BIGINT NULL,
  `store_role` VARCHAR(255) NULL,
  `physical_size` BIGINT DEFAULT '0' NULL,
  `download_state` VARCHAR(255) NULL,
  `error_str` VARCHAR(255) NULL,
  `local_path` VARCHAR(255) NULL,
  `install_path` VARCHAR(255) NULL,
  `url` VARCHAR(2048) NULL,
  `state` VARCHAR(255) NOT NULL,
  `destroyed` TINYINT(1) NULL
  COMMENT 'indicates whether the template_store entry was destroyed by the user or not',
  `is_copy` TINYINT(1) DEFAULT '0' NOT NULL
  COMMENT 'indicates whether this was copied',
  `update_count` BIGINT NULL,
  `ref_cnt` BIGINT DEFAULT '0' NULL,
  `updated` DATETIME NULL,
  `download_url_created` DATETIME NULL,
  `download_url` VARCHAR(2048) NULL
);

CREATE INDEX `i_template_store_ref__store_id`
  ON `template_store_ref` (`store_id`);

CREATE INDEX `i_template_store_ref__template_id`
  ON `template_store_ref` (`template_id`);

CREATE TABLE `template_zone_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `zone_id` BIGINT NOT NULL,
  `template_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  CONSTRAINT `fk_template_zone_ref__zone_id`
  FOREIGN KEY (`zone_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_template_zone_ref__removed`
  ON `template_zone_ref` (`removed`);

CREATE INDEX `i_template_zone_ref__template_id`
  ON `template_zone_ref` (`template_id`);

CREATE INDEX `i_template_zone_ref__zone_id`
  ON `template_zone_ref` (`zone_id`);

CREATE TABLE `ucs_blade` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `ucs_manager_id` BIGINT NOT NULL,
  `host_id` BIGINT NULL,
  `dn` VARCHAR(512) NOT NULL,
  `profile_dn` VARCHAR(512) NULL,
  CONSTRAINT `uuid`
  UNIQUE (`uuid`)
);

CREATE TABLE `ucs_manager` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `zone_id` BIGINT NOT NULL,
  `name` VARCHAR(128) NULL,
  `url` VARCHAR(255) NOT NULL,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  CONSTRAINT `uuid`
  UNIQUE (`uuid`)
);

CREATE TABLE `upload` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `host_id` BIGINT NOT NULL,
  `type_id` BIGINT NOT NULL,
  `type` VARCHAR(255) NULL,
  `mode` VARCHAR(255) NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `upload_pct` INT(10) UNSIGNED NULL,
  `upload_state` VARCHAR(255) NULL,
  `error_str` VARCHAR(255) NULL,
  `url` VARCHAR(255) NULL,
  `install_path` VARCHAR(255) NULL,
  `uuid` VARCHAR(40) NULL,
  CONSTRAINT `fk_upload__store_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`image_store` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_upload__host_id`
  ON `upload` (`host_id`);

CREATE INDEX `i_upload__type_id`
  ON `upload` (`type_id`);

CREATE TABLE `usage_event` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `type` VARCHAR(32) NOT NULL,
  `account_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `zone_id` BIGINT NOT NULL,
  `resource_id` BIGINT NULL,
  `resource_name` VARCHAR(255) NULL,
  `offering_id` BIGINT NULL,
  `template_id` BIGINT NULL,
  `size` BIGINT NULL,
  `resource_type` VARCHAR(32) NULL,
  `processed` TINYINT DEFAULT '0' NOT NULL,
  `virtual_size` BIGINT NULL
);

CREATE INDEX `i_usage_event__created`
  ON `usage_event` (`created`);

CREATE TABLE `usage_event_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `usage_event_id` BIGINT NOT NULL
  COMMENT 'usage event id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  CONSTRAINT `fk_usage_event_details__usage_event_id`
  FOREIGN KEY (`usage_event_id`) REFERENCES `cloud`.`usage_event` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_usage_event_details__usage_event_id`
  ON `usage_event_details` (`usage_event_id`);

CREATE TABLE `user` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `account_id` BIGINT NOT NULL,
  `firstname` VARCHAR(255) NULL,
  `lastname` VARCHAR(255) NULL,
  `email` VARCHAR(255) NULL,
  `state` VARCHAR(10) DEFAULT 'enabled' NOT NULL,
  `api_key` VARCHAR(255) NULL,
  `secret_key` VARCHAR(255) NULL,
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `timezone` VARCHAR(30) NULL,
  `registration_token` VARCHAR(255) NULL,
  `is_registered` TINYINT DEFAULT '0' NOT NULL
  COMMENT '1: yes, 0: no',
  `incorrect_login_attempts` INT(10) UNSIGNED DEFAULT '0' NOT NULL,
  `default` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if user is default',
  `source` VARCHAR(40) DEFAULT 'UNKNOWN' NOT NULL,
  `external_entity` TEXT NULL
  COMMENT 'reference to external federation entity',
  CONSTRAINT `uc_user__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `i_user__api_key`
  UNIQUE (`api_key`),
  CONSTRAINT `fk_user__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_user__account_id`
  ON `user` (`account_id`);

CREATE INDEX `i_user__removed`
  ON `user` (`removed`);

CREATE INDEX `i_user__secret_key_removed`
  ON `user` (`secret_key`, `removed`);

ALTER TABLE `autoscale_vmprofiles`
  ADD CONSTRAINT `fk_autoscale_vmprofiles__autoscale_user_id`
FOREIGN KEY (`autoscale_user_id`) REFERENCES `cloud`.`user` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `user_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `user_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_user_details__user_id`
  FOREIGN KEY (`user_id`) REFERENCES `cloud`.`user` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_user_details__user_id`
  ON `user_details` (`user_id`);

CREATE TABLE `user_ip_address` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `account_id` BIGINT NULL,
  `domain_id` BIGINT NULL,
  `public_ip_address` CHAR(40) NOT NULL,
  `data_center_id` BIGINT NOT NULL
  COMMENT 'zone that it belongs to',
  `source_nat` INT(1) UNSIGNED DEFAULT '0' NOT NULL,
  `allocated` DATETIME NULL
  COMMENT 'Date this ip was allocated to someone',
  `vlan_db_id` BIGINT NOT NULL,
  `one_to_one_nat` INT(1) UNSIGNED DEFAULT '0' NOT NULL,
  `vm_id` BIGINT NULL
  COMMENT 'vm id the one_to_one nat ip is assigned to',
  `state` CHAR(32) DEFAULT 'Free' NOT NULL
  COMMENT 'state of the ip address',
  `mac_address` BIGINT NOT NULL
  COMMENT 'mac address of this ip',
  `source_network_id` BIGINT NOT NULL
  COMMENT 'network id ip belongs to',
  `network_id` BIGINT NULL
  COMMENT 'network this public ip address is associated with',
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'physical network id that this configuration is based on',
  `ip_acl_id` BIGINT NOT NULL,
  `is_system` INT(1) UNSIGNED DEFAULT '0' NOT NULL,
  `vpc_id` BIGINT NULL
  COMMENT 'vpc the ip address is associated with',
  `dnat_vmip` VARCHAR(40) NULL,
  `is_portable` INT(1) UNSIGNED DEFAULT '0' NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the ip address can be displayed to the end user',
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `uc_user_ip_address__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `public_ip_address`
  UNIQUE (`public_ip_address`, `source_network_id`, `removed`),
  CONSTRAINT `fk_user_ip_address__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`),
  CONSTRAINT `fk_user_ip_address__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_user_ip_address__source_network_id`
  FOREIGN KEY (`source_network_id`) REFERENCES `cloud`.`networks` (`id`),
  CONSTRAINT `fk_user_ip_address__network_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`),
  CONSTRAINT `fk_user_ip_address__physical_network_id`
  FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_user_ip_address__account_id`
  ON `user_ip_address` (`account_id`);

CREATE INDEX `i_user_ip_address__data_center_id`
  ON `user_ip_address` (`data_center_id`);

CREATE INDEX `i_user_ip_address__network_id`
  ON `user_ip_address` (`network_id`);

CREATE INDEX `i_user_ip_address__physical_network_id`
  ON `user_ip_address` (`physical_network_id`);

CREATE INDEX `i_user_ip_address__source_network_id`
  ON `user_ip_address` (`source_network_id`);

CREATE INDEX `i_user_ip_address__vlan_db_id`
  ON `user_ip_address` (`vlan_db_id`);

CREATE INDEX `i_user_ip_address__vm_id`
  ON `user_ip_address` (`vm_id`);

CREATE INDEX `i_user_ip_address__vpc_id`
  ON `user_ip_address` (`vpc_id`);

CREATE INDEX `i_user_ip_address__allocated`
  ON `user_ip_address` (`allocated`);

CREATE INDEX `i_user_ip_address__source_nat`
  ON `user_ip_address` (`source_nat`);

ALTER TABLE `firewall_rules`
  ADD CONSTRAINT `fk_firewall_rules__ip_address_id`
FOREIGN KEY (`ip_address_id`) REFERENCES `cloud`.`user_ip_address` (`id`);

ALTER TABLE `remote_access_vpn`
  ADD CONSTRAINT `fk_remote_access_vpn__vpn_server_addr_id`
FOREIGN KEY (`vpn_server_addr_id`) REFERENCES `cloud`.`user_ip_address` (`id`);

ALTER TABLE `s2s_vpn_gateway`
  ADD CONSTRAINT `fk_s2s_vpn_gateway__addr_id`
FOREIGN KEY (`addr_id`) REFERENCES `cloud`.`user_ip_address` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `user_ip_address_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `user_ip_address_id` BIGINT NOT NULL
  COMMENT 'User ip address id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_user_ip_address_details__user_ip_address_id`
  FOREIGN KEY (`user_ip_address_id`) REFERENCES `cloud`.`user_ip_address` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_user_ip_address_details__user_ip_address_id`
  ON `user_ip_address_details` (`user_ip_address_id`);

CREATE TABLE `user_ipv6_address` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `account_id` BIGINT NULL,
  `domain_id` BIGINT NULL,
  `ip_address` CHAR(50) NOT NULL,
  `data_center_id` BIGINT NOT NULL
  COMMENT 'zone that it belongs to',
  `vlan_id` BIGINT NOT NULL,
  `state` CHAR(32) DEFAULT 'Free' NOT NULL
  COMMENT 'state of the ip address',
  `mac_address` VARCHAR(40) NOT NULL
  COMMENT 'mac address of this ip',
  `source_network_id` BIGINT NOT NULL
  COMMENT 'network id ip belongs to',
  `network_id` BIGINT NULL
  COMMENT 'network this public ip address is associated with',
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'physical network id that this configuration is based on',
  `created` DATETIME NULL
  COMMENT 'Date this ip was allocated to someone',
  CONSTRAINT `uc_user_ipv6_address__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `ip_address`
  UNIQUE (`ip_address`, `source_network_id`),
  CONSTRAINT `fk_user_ipv6_address__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`),
  CONSTRAINT `fk_user_ipv6_address__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_user_ipv6_address__source_network_id`
  FOREIGN KEY (`source_network_id`) REFERENCES `cloud`.`networks` (`id`),
  CONSTRAINT `fk_user_ipv6_address__network_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`),
  CONSTRAINT `fk_user_ipv6_address__physical_network_id`
  FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_user_ipv6_address__account_id`
  ON `user_ipv6_address` (`account_id`);

CREATE INDEX `i_user_ipv6_address__data_center_id`
  ON `user_ipv6_address` (`data_center_id`);

CREATE INDEX `i_user_ipv6_address__network_id`
  ON `user_ipv6_address` (`network_id`);

CREATE INDEX `i_user_ipv6_address__physical_network_id`
  ON `user_ipv6_address` (`physical_network_id`);

CREATE INDEX `i_user_ipv6_address__source_network_id`
  ON `user_ipv6_address` (`source_network_id`);

CREATE INDEX `i_user_ipv6_address__vlan_id`
  ON `user_ipv6_address` (`vlan_id`);

CREATE TABLE `user_statistics` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `data_center_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `public_ip_address` CHAR(40) NULL,
  `device_id` BIGINT NOT NULL,
  `device_type` VARCHAR(32) NOT NULL,
  `network_id` BIGINT NULL,
  `net_bytes_received` BIGINT DEFAULT '0' NOT NULL,
  `net_bytes_sent` BIGINT DEFAULT '0' NOT NULL,
  `current_bytes_received` BIGINT DEFAULT '0' NOT NULL,
  `current_bytes_sent` BIGINT DEFAULT '0' NOT NULL,
  `agg_bytes_received` BIGINT DEFAULT '0' NOT NULL,
  `agg_bytes_sent` BIGINT DEFAULT '0' NOT NULL,
  CONSTRAINT `account_id`
  UNIQUE (`account_id`, `data_center_id`, `public_ip_address`, `device_id`, `device_type`),
  CONSTRAINT `fk_user_statistics__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_user_statistics__account_id`
  ON `user_statistics` (`account_id`);

CREATE INDEX `i_user_statistics__account_id_data_center_id`
  ON `user_statistics` (`account_id`, `data_center_id`);

CREATE TABLE `user_vm` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `iso_id` BIGINT NULL,
  `display_name` VARCHAR(255) NULL,
  `user_data` MEDIUMTEXT NULL,
  `update_parameters` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'Defines if the parameters have been updated for the vm'
);

ALTER TABLE `affinity_group_vm_map`
  ADD CONSTRAINT `fk_affinity_group_vm_map___instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`user_vm` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `instance_group_vm_map`
  ADD CONSTRAINT `fk_instance_group_vm_map___instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`user_vm` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `security_group_vm_map`
  ADD CONSTRAINT `fk_security_group_vm_map___instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`user_vm` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `user_vm_clone_setting` (
  `vm_id` BIGINT NOT NULL
  COMMENT 'guest VM id'
    PRIMARY KEY,
  `clone_type` VARCHAR(10) NOT NULL
  COMMENT 'Full or Linked Clone (applicable to VMs on ESX)'
);

CREATE TABLE `user_vm_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vm_id` BIGINT NOT NULL
  COMMENT 'vm id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(5120) NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_user_vm_details__vm_id`
  ON `user_vm_details` (`vm_id`);

CREATE INDEX `i_name_vm_id`
  ON `user_vm_details` (`vm_id`, `name`);

CREATE TABLE `version` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `version` CHAR(40) NOT NULL
  COMMENT 'version',
  `updated` DATETIME NOT NULL
  COMMENT 'Date this version table was updated',
  `step` CHAR(32) NOT NULL
  COMMENT 'Step in the upgrade to this version',
  CONSTRAINT `version`
  UNIQUE (`version`)
);

CREATE INDEX `i_version__version`
  ON `version` (`version`);

CREATE TABLE `vgpu_types` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `gpu_group_id` BIGINT NOT NULL,
  `vgpu_type` VARCHAR(40) NOT NULL
  COMMENT 'vgpu type supported by this gpu group',
  `video_ram` BIGINT NULL
  COMMENT 'video RAM for this vgpu type',
  `max_heads` BIGINT NULL
  COMMENT 'maximum displays per user',
  `max_resolution_x` BIGINT NULL
  COMMENT 'maximum X resolution per display',
  `max_resolution_y` BIGINT NULL
  COMMENT 'maximum Y resolution per display',
  `max_vgpu_per_pgpu` BIGINT NULL
  COMMENT 'max number of vgpus per physical gpu (pgpu)',
  `remaining_capacity` BIGINT NULL
  COMMENT 'remaining vgpu can be created with this vgpu_type on the given gpu group',
  `max_capacity` BIGINT NULL
  COMMENT 'maximum vgpu can be created with this vgpu_type on the given gpu group',
  CONSTRAINT `fk_vgpu_types__gpu_group_id`
  FOREIGN KEY (`gpu_group_id`) REFERENCES `cloud`.`host_gpu_groups` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vgpu_types__gpu_group_id`
  ON `vgpu_types` (`gpu_group_id`);

CREATE TABLE `virtual_router_providers` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `nsp_id` BIGINT NOT NULL
  COMMENT 'Network Service Provider ID',
  `uuid` VARCHAR(40) NULL,
  `type` VARCHAR(255) NOT NULL
  COMMENT 'Virtual router, or ElbVM',
  `enabled` INT(1) NOT NULL
  COMMENT 'Enabled or disabled',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  CONSTRAINT `uc_virtual_router_providers__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_virtual_router_providers__nsp_id`
  FOREIGN KEY (`nsp_id`) REFERENCES `cloud`.`physical_network_service_providers` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_virtual_router_providers__nsp_id`
  ON `virtual_router_providers` (`nsp_id`);

ALTER TABLE `domain_router`
  ADD CONSTRAINT `fk_domain_router__element_id`
FOREIGN KEY (`element_id`) REFERENCES `cloud`.`virtual_router_providers` (`id`);

CREATE TABLE `virtual_supervisor_module` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `host_id` BIGINT NOT NULL,
  `vsm_name` VARCHAR(255) NULL,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `ipaddr` VARCHAR(80) NOT NULL,
  `management_vlan` INT(32) NULL,
  `control_vlan` INT(32) NULL,
  `packet_vlan` INT(32) NULL,
  `storage_vlan` INT(32) NULL,
  `vsm_domain_id` BIGINT NULL,
  `config_mode` VARCHAR(20) NULL,
  `config_state` VARCHAR(20) NULL,
  `vsm_device_state` VARCHAR(20) NOT NULL
);

CREATE TABLE `vlan` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `vlan_id` VARCHAR(255) NULL,
  `vlan_gateway` VARCHAR(255) NULL,
  `vlan_netmask` VARCHAR(255) NULL,
  `description` VARCHAR(255) NULL,
  `vlan_type` VARCHAR(255) NULL,
  `data_center_id` BIGINT NOT NULL,
  `network_id` BIGINT NOT NULL
  COMMENT 'id of corresponding network offering',
  `physical_network_id` BIGINT NOT NULL
  COMMENT 'physical network id that this configuration is based on',
  `ip6_gateway` VARCHAR(255) NULL,
  `ip6_cidr` VARCHAR(255) NULL,
  `ip6_range` VARCHAR(255) NULL,
  `removed` DATETIME NULL
  COMMENT 'date removed',
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `uc_vlan__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_vlan__data_center_id`
  FOREIGN KEY (`data_center_id`) REFERENCES `cloud`.`data_center` (`id`),
  CONSTRAINT `fk_vlan__physical_network_id`
  FOREIGN KEY (`physical_network_id`) REFERENCES `cloud`.`physical_network` (`id`)
);

CREATE INDEX `i_vlan__data_center_id`
  ON `vlan` (`data_center_id`);

CREATE INDEX `i_vlan__physical_network_id`
  ON `vlan` (`physical_network_id`);

ALTER TABLE `account_vlan_map`
  ADD CONSTRAINT `fk_account_vlan_map__vlan_id`
FOREIGN KEY (`vlan_db_id`) REFERENCES `cloud`.`vlan` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `domain_vlan_map`
  ADD CONSTRAINT `fk_domain_vlan_map__vlan_id`
FOREIGN KEY (`vlan_db_id`) REFERENCES `cloud`.`vlan` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `pod_vlan_map`
  ADD CONSTRAINT `fk_pod_vlan_map__vlan_id`
FOREIGN KEY (`vlan_db_id`) REFERENCES `cloud`.`vlan` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `user_ip_address`
  ADD CONSTRAINT `fk_user_ip_address__vlan_db_id`
FOREIGN KEY (`vlan_db_id`) REFERENCES `cloud`.`vlan` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `user_ipv6_address`
  ADD CONSTRAINT `fk_user_ipv6_address__vlan_id`
FOREIGN KEY (`vlan_id`) REFERENCES `cloud`.`vlan` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `vm_compute_tags` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `vm_id` BIGINT NOT NULL
  COMMENT 'vm id',
  `compute_tag` VARCHAR(255) NOT NULL
  COMMENT 'name of tag'
);

CREATE INDEX `i_vm_id`
  ON `vm_compute_tags` (`vm_id`);

CREATE TABLE `vm_disk_statistics` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `data_center_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `vm_id` BIGINT NOT NULL,
  `volume_id` BIGINT DEFAULT '0' NOT NULL,
  `net_io_read` BIGINT DEFAULT '0' NOT NULL,
  `net_io_write` BIGINT DEFAULT '0' NOT NULL,
  `current_io_read` BIGINT DEFAULT '0' NOT NULL,
  `current_io_write` BIGINT DEFAULT '0' NOT NULL,
  `agg_io_read` BIGINT DEFAULT '0' NOT NULL,
  `agg_io_write` BIGINT DEFAULT '0' NOT NULL,
  `net_bytes_read` BIGINT DEFAULT '0' NOT NULL,
  `net_bytes_write` BIGINT DEFAULT '0' NOT NULL,
  `current_bytes_read` BIGINT DEFAULT '0' NOT NULL,
  `current_bytes_write` BIGINT DEFAULT '0' NOT NULL,
  `agg_bytes_read` BIGINT DEFAULT '0' NOT NULL,
  `agg_bytes_write` BIGINT DEFAULT '0' NOT NULL,
  CONSTRAINT `account_id`
  UNIQUE (`account_id`, `data_center_id`, `vm_id`, `volume_id`),
  CONSTRAINT `fk_vm_disk_statistics__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vm_disk_statistics__account_id`
  ON `vm_disk_statistics` (`account_id`);

CREATE INDEX `i_vm_disk_statistics__account_id_data_center_id`
  ON `vm_disk_statistics` (`account_id`, `data_center_id`);

CREATE TABLE `vm_instance` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL,
  `instance_name` VARCHAR(255) NOT NULL
  COMMENT 'name of the vm instance running on the hosts',
  `state` VARCHAR(32) NOT NULL,
  `vm_template_id` BIGINT NULL,
  `guest_os_id` BIGINT NOT NULL,
  `private_mac_address` VARCHAR(17) NULL,
  `private_ip_address` CHAR(40) NULL,
  `pod_id` BIGINT NULL,
  `data_center_id` BIGINT NOT NULL
  COMMENT 'Data Center the instance belongs to',
  `host_id` BIGINT NULL,
  `last_host_id` BIGINT NULL
  COMMENT 'tentative host for first run or last host that it has been running on',
  `proxy_id` BIGINT NULL
  COMMENT 'console proxy allocated in previous session',
  `proxy_assign_time` DATETIME NULL
  COMMENT 'time when console proxy was assigned',
  `vnc_password` VARCHAR(255) NOT NULL
  COMMENT 'vnc password',
  `ha_enabled` TINYINT(1) DEFAULT '0' NOT NULL
  COMMENT 'Should HA be enabled for this VM',
  `limit_cpu_use` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Limit the cpu usage to service offering',
  `update_count` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'date state was updated',
  `update_time` DATETIME NULL
  COMMENT 'date the destroy was requested',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `type` VARCHAR(32) NOT NULL
  COMMENT 'type of vm it is',
  `vm_type` VARCHAR(32) NOT NULL
  COMMENT 'vm type',
  `account_id` BIGINT NOT NULL
  COMMENT 'user id of owner',
  `domain_id` BIGINT NOT NULL,
  `service_offering_id` BIGINT NOT NULL
  COMMENT 'service offering id',
  `reservation_id` CHAR(40) NULL
  COMMENT 'reservation id',
  `hypervisor_type` CHAR(32) NULL
  COMMENT 'hypervisor type',
  `disk_offering_id` BIGINT NULL,
  `owner` VARCHAR(255) NULL,
  `host_name` VARCHAR(255) NULL,
  `display_name` VARCHAR(255) NULL,
  `desired_state` VARCHAR(32) NULL,
  `dynamically_scalable` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if VM contains XS/VMWare tools inorder to support dynamic scaling of VM cpu/memory',
  `display_vm` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'Should vm instance be displayed to the end user',
  `power_state` VARCHAR(74) DEFAULT 'PowerUnknown' NULL,
  `power_state_update_time` DATETIME NULL,
  `power_state_update_count` INT DEFAULT '0' NULL,
  `power_host` BIGINT NULL,
  `user_id` BIGINT DEFAULT '1' NOT NULL
  COMMENT 'user id of VM deployer',
  CONSTRAINT `uc_vm_instance_uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_vm_instance__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`),
  CONSTRAINT `fk_vm_instance__last_host_id`
  FOREIGN KEY (`last_host_id`) REFERENCES `cloud`.`host` (`id`),
  CONSTRAINT `fk_vm_instance__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`),
  CONSTRAINT `fk_vm_instance__service_offering_id`
  FOREIGN KEY (`service_offering_id`) REFERENCES `cloud`.`service_offering` (`id`),
  CONSTRAINT `fk_vm_instance__power_host`
  FOREIGN KEY (`power_host`) REFERENCES `cloud`.`host` (`id`)
);

CREATE INDEX `i_vm_instance__account_id`
  ON `vm_instance` (`account_id`);

CREATE INDEX `i_vm_instance__host_id`
  ON `vm_instance` (`host_id`);

CREATE INDEX `i_vm_instance__last_host_id`
  ON `vm_instance` (`last_host_id`);

CREATE INDEX `i_vm_instance__power_host`
  ON `vm_instance` (`power_host`);

CREATE INDEX `i_vm_instance__service_offering_id`
  ON `vm_instance` (`service_offering_id`);

CREATE INDEX `i_vm_instance__data_center_id`
  ON `vm_instance` (`data_center_id`);

CREATE INDEX `i_vm_instance__instance_name`
  ON `vm_instance` (`instance_name`);

CREATE INDEX `i_vm_instance__pod_id`
  ON `vm_instance` (`pod_id`);

CREATE INDEX `i_vm_instance__removed`
  ON `vm_instance` (`removed`);

CREATE INDEX `i_vm_instance__state`
  ON `vm_instance` (`state`);

CREATE INDEX `i_vm_instance__template_id`
  ON `vm_instance` (`vm_template_id`);

CREATE INDEX `i_vm_instance__type`
  ON `vm_instance` (`type`);

CREATE INDEX `i_vm_instance__update_count`
  ON `vm_instance` (`update_count`);

CREATE INDEX `i_vm_instance__update_time`
  ON `vm_instance` (`update_time`);

ALTER TABLE `autoscale_vmgroup_vm_map`
  ADD CONSTRAINT `fk_autoscale_vmgroup_vm_map__instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`vm_instance` (`id`);

ALTER TABLE `console_proxy`
  ADD CONSTRAINT `fk_console_proxy__id`
FOREIGN KEY (`id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `domain_router`
  ADD CONSTRAINT `fk_domain_router__id`
FOREIGN KEY (`id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `load_balancer_vm_map`
  ADD CONSTRAINT `fk_load_balancer_vm_map__instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `nic_secondary_ips`
  ADD CONSTRAINT `fk_nic_secondary_ip__vmId`
FOREIGN KEY (`vmId`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `nics`
  ADD CONSTRAINT `fk_nics__instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `op_ha_work`
  ADD CONSTRAINT `fk_op_ha_work__instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `op_it_work`
  ADD CONSTRAINT `fk_op_it_work__instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `op_router_monitoring_services`
  ADD CONSTRAINT `fk_virtual_router__id`
FOREIGN KEY (`vm_id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `port_forwarding_rules`
  ADD CONSTRAINT `fk_port_forwarding_rules__instance_id`
FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `secondary_storage_vm`
  ADD CONSTRAINT `fk_secondary_storage_vm__id`
FOREIGN KEY (`id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `user_ip_address`
  ADD CONSTRAINT `fk_user_ip_address__vm_id`
FOREIGN KEY (`vm_id`) REFERENCES `cloud`.`vm_instance` (`id`);

ALTER TABLE `user_vm`
  ADD CONSTRAINT `fk_user_vm__id`
FOREIGN KEY (`id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `user_vm_details`
  ADD CONSTRAINT `fk_user_vm_details__vm_id`
FOREIGN KEY (`vm_id`) REFERENCES `cloud`.`vm_instance` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `vm_network_map` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `vm_id` BIGINT NOT NULL
  COMMENT 'vm id',
  `network_id` BIGINT NOT NULL
  COMMENT 'network id'
);

CREATE INDEX `i_vm_id`
  ON `vm_network_map` (`vm_id`);

CREATE TABLE `vm_reservation` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NOT NULL
  COMMENT 'reservation id',
  `vm_id` BIGINT NOT NULL
  COMMENT 'vm id',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'zone id',
  `pod_id` BIGINT NOT NULL
  COMMENT 'pod id',
  `cluster_id` BIGINT NOT NULL
  COMMENT 'cluster id',
  `host_id` BIGINT NOT NULL
  COMMENT 'host id',
  `created` DATETIME NULL
  COMMENT 'date created',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `deployment_planner` VARCHAR(40) NULL
  COMMENT 'Preferred deployment planner for the vm',
  CONSTRAINT `uc_vm_reservation__uuid`
  UNIQUE (`uuid`)
);

CREATE TABLE `vm_root_disk_tags` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `vm_id` BIGINT NOT NULL
  COMMENT 'vm id',
  `root_disk_tag` VARCHAR(255) NOT NULL
  COMMENT 'name of tag'
);

CREATE INDEX `i_vm_id`
  ON `vm_root_disk_tags` (`vm_id`);

CREATE TABLE `vm_snapshot_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vm_snapshot_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE TABLE `vm_snapshots` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'Primary Key'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `display_name` VARCHAR(255) NULL,
  `description` VARCHAR(255) NULL,
  `vm_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `vm_snapshot_type` VARCHAR(32) NULL,
  `state` VARCHAR(32) NOT NULL,
  `parent` BIGINT NULL,
  `current` INT(1) UNSIGNED NULL,
  `update_count` BIGINT DEFAULT '0' NOT NULL,
  `updated` DATETIME NULL,
  `created` DATETIME NULL,
  `removed` DATETIME NULL,
  CONSTRAINT `uc_vm_snapshots_uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_vm_snapshots_vm_id__vm_instance_id`
  FOREIGN KEY (`vm_id`) REFERENCES `cloud`.`vm_instance` (`id`),
  CONSTRAINT `fk_vm_snapshots_account_id__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`),
  CONSTRAINT `fk_vm_snapshots_domain_id__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
);

CREATE INDEX `i_vm_snapshots_domain_id__domain_id`
  ON `vm_snapshots` (`domain_id`);

CREATE INDEX `vm_snapshots_account_id`
  ON `vm_snapshots` (`account_id`);

CREATE INDEX `vm_snapshots_display_name`
  ON `vm_snapshots` (`display_name`);

CREATE INDEX `vm_snapshots_name`
  ON `vm_snapshots` (`name`);

CREATE INDEX `vm_snapshots_parent`
  ON `vm_snapshots` (`parent`);

CREATE INDEX `vm_snapshots_removed`
  ON `vm_snapshots` (`removed`);

CREATE INDEX `vm_snapshots_vm_id`
  ON `vm_snapshots` (`vm_id`);

CREATE TABLE `vm_template` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `unique_name` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `uuid` VARCHAR(40) NULL,
  `public` INT(1) UNSIGNED NOT NULL,
  `featured` INT(1) UNSIGNED NOT NULL,
  `type` VARCHAR(32) NULL,
  `hvm` INT(1) UNSIGNED NOT NULL
  COMMENT 'requires HVM',
  `bits` INT(6) UNSIGNED NOT NULL
  COMMENT '32 bit or 64 bit',
  `url` VARCHAR(255) NULL
  COMMENT 'the url where the template exists externally',
  `format` VARCHAR(32) NOT NULL
  COMMENT 'format for the template',
  `created` DATETIME NOT NULL
  COMMENT 'Date created',
  `removed` DATETIME NULL
  COMMENT 'Date removed if not null',
  `account_id` BIGINT NOT NULL
  COMMENT 'id of the account that created this template',
  `checksum` VARCHAR(255) NULL
  COMMENT 'checksum for the template root disk',
  `display_text` VARCHAR(4096) NULL
  COMMENT 'Description text set by the admin for display purpose only',
  `enable_password` INT(1) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'true if this template supports password reset',
  `enable_sshkey` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if this template supports sshkey reset',
  `guest_os_id` BIGINT NOT NULL
  COMMENT 'the OS of the template',
  `bootable` INT(1) UNSIGNED DEFAULT '1' NOT NULL
  COMMENT 'true if this template represents a bootable ISO',
  `prepopulate` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'prepopulate this template to primary storage',
  `cross_zones` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Make this template available in all zones',
  `extractable` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is this template extractable',
  `hypervisor_type` VARCHAR(32) NULL
  COMMENT 'hypervisor that the template belongs to',
  `source_template_id` BIGINT NULL
  COMMENT 'Id of the original template, if this template is created from snapshot',
  `template_tag` VARCHAR(255) NULL
  COMMENT 'template tag',
  `sort_key` INT(32) DEFAULT '0' NOT NULL
  COMMENT 'sort key used for customising sort method',
  `size` BIGINT NULL,
  `state` VARCHAR(255) NULL,
  `update_count` BIGINT NULL,
  `updated` DATETIME NULL,
  `dynamically_scalable` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'true if template contains XS/VMWare tools inorder to support dynamic scaling of VM cpu/memory',
  CONSTRAINT `uc_vm_template__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_vm_template__public`
  ON `vm_template` (`public`);

CREATE INDEX `i_vm_template__removed`
  ON `vm_template` (`removed`);

ALTER TABLE `template_host_ref`
  ADD CONSTRAINT `fk_template_host_ref__template_id`
FOREIGN KEY (`template_id`) REFERENCES `cloud`.`vm_template` (`id`);

ALTER TABLE `template_spool_ref`
  ADD CONSTRAINT `fk_template_spool_ref__template_id`
FOREIGN KEY (`template_id`) REFERENCES `cloud`.`vm_template` (`id`);

ALTER TABLE `template_store_ref`
  ADD CONSTRAINT `fk_template_store_ref__template_id`
FOREIGN KEY (`template_id`) REFERENCES `cloud`.`vm_template` (`id`);

ALTER TABLE `template_zone_ref`
  ADD CONSTRAINT `fk_template_zone_ref__template_id`
FOREIGN KEY (`template_id`) REFERENCES `cloud`.`vm_template` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `vm_instance`
  ADD CONSTRAINT `fk_vm_instance__template_id`
FOREIGN KEY (`vm_template_id`) REFERENCES `cloud`.`vm_template` (`id`);

CREATE TABLE `vm_template_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `template_id` BIGINT NOT NULL
  COMMENT 'template id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_vm_template_details__template_id`
  FOREIGN KEY (`template_id`) REFERENCES `cloud`.`vm_template` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vm_template_details__template_id`
  ON `vm_template_details` (`template_id`);

CREATE TABLE `vm_work_job` (
  `id` BIGINT NOT NULL
    PRIMARY KEY,
  `step` CHAR(32) NOT NULL
  COMMENT 'state',
  `vm_type` CHAR(32) NOT NULL
  COMMENT 'type of vm',
  `vm_instance_id` BIGINT NOT NULL
  COMMENT 'vm instance',
  CONSTRAINT `fk_vm_work_job__instance_id`
  FOREIGN KEY (`vm_instance_id`) REFERENCES `cloud`.`vm_instance` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vm_work_job__instance_id`
  ON `vm_work_job` (`vm_instance_id`);

CREATE INDEX `i_vm_work_job__step`
  ON `vm_work_job` (`step`);

CREATE INDEX `i_vm_work_job__vm`
  ON `vm_work_job` (`vm_type`, `vm_instance_id`);

CREATE TABLE `vmware_data_center` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(255) NULL,
  `name` VARCHAR(255) NOT NULL
  COMMENT 'Name of VMware datacenter',
  `guid` VARCHAR(255) NOT NULL
  COMMENT 'id of VMware datacenter',
  `vcenter_host` VARCHAR(255) NOT NULL
  COMMENT 'vCenter host containing this VMware datacenter',
  `username` VARCHAR(255) NOT NULL
  COMMENT 'Name of vCenter host user',
  `password` VARCHAR(255) NOT NULL
  COMMENT 'Password of vCenter host user',
  CONSTRAINT `uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `guid`
  UNIQUE (`guid`)
);

CREATE TABLE `vmware_data_center_zone_map` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `zone_id` BIGINT NOT NULL
  COMMENT 'id of CloudStack zone',
  `vmware_data_center_id` BIGINT NOT NULL
  COMMENT 'id of VMware datacenter',
  CONSTRAINT `zone_id`
  UNIQUE (`zone_id`),
  CONSTRAINT `vmware_data_center_id`
  UNIQUE (`vmware_data_center_id`),
  CONSTRAINT `fk_vmware_data_center_zone_map__vmware_data_center_id`
  FOREIGN KEY (`vmware_data_center_id`) REFERENCES `cloud`.`vmware_data_center` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `volume_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `volume_id` BIGINT NOT NULL
  COMMENT 'volume id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_volume_details__volume_id`
  ON `volume_details` (`volume_id`);

CREATE TABLE `volume_host_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `host_id` BIGINT NOT NULL,
  `volume_id` BIGINT NOT NULL,
  `zone_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `download_pct` INT(10) UNSIGNED NULL,
  `size` BIGINT NULL,
  `physical_size` BIGINT DEFAULT '0' NULL,
  `download_state` VARCHAR(255) NULL,
  `checksum` VARCHAR(255) NULL
  COMMENT 'checksum for the data disk',
  `error_str` VARCHAR(255) NULL,
  `local_path` VARCHAR(255) NULL,
  `install_path` VARCHAR(255) NULL,
  `url` VARCHAR(2048) NULL,
  `format` VARCHAR(32) NOT NULL
  COMMENT 'format for the volume',
  `destroyed` TINYINT(1) NULL
  COMMENT 'indicates whether the volume_host entry was destroyed by the user or not',
  CONSTRAINT `fk_volume_host_ref__host_id`
  FOREIGN KEY (`host_id`) REFERENCES `cloud`.`host` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_volume_host_ref__host_id`
  ON `volume_host_ref` (`host_id`);

CREATE INDEX `i_volume_host_ref__volume_id`
  ON `volume_host_ref` (`volume_id`);

CREATE TABLE `volume_reservation` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `vm_reservation_id` BIGINT NOT NULL
  COMMENT 'id of the vm reservation',
  `vm_id` BIGINT NOT NULL
  COMMENT 'vm id',
  `volume_id` BIGINT NOT NULL
  COMMENT 'volume id',
  `pool_id` BIGINT NOT NULL
  COMMENT 'pool assigned to the volume',
  CONSTRAINT `fk_vm_pool_reservation__vm_reservation_id`
  FOREIGN KEY (`vm_reservation_id`) REFERENCES `cloud`.`vm_reservation` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vm_pool_reservation__vm_reservation_id`
  ON `volume_reservation` (`vm_reservation_id`);

CREATE TABLE `volume_store_ref` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `store_id` BIGINT NOT NULL,
  `volume_id` BIGINT NOT NULL,
  `zone_id` BIGINT NOT NULL,
  `created` DATETIME NOT NULL,
  `last_updated` DATETIME NULL,
  `job_id` VARCHAR(255) NULL,
  `download_pct` INT(10) UNSIGNED NULL,
  `size` BIGINT NULL,
  `physical_size` BIGINT DEFAULT '0' NULL,
  `download_state` VARCHAR(255) NULL,
  `checksum` VARCHAR(255) NULL
  COMMENT 'checksum for the data disk',
  `error_str` VARCHAR(255) NULL,
  `local_path` VARCHAR(255) NULL,
  `install_path` VARCHAR(255) NULL,
  `url` VARCHAR(2048) NULL,
  `download_url` VARCHAR(2048) NULL,
  `state` VARCHAR(255) NOT NULL,
  `destroyed` TINYINT(1) NULL
  COMMENT 'indicates whether the volume_host entry was destroyed by the user or not',
  `update_count` BIGINT NULL,
  `ref_cnt` BIGINT NULL,
  `updated` DATETIME NULL,
  `download_url_created` DATETIME NULL,
  CONSTRAINT `fk_volume_store_ref__store_id`
  FOREIGN KEY (`store_id`) REFERENCES `cloud`.`image_store` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_volume_store_ref__store_id`
  ON `volume_store_ref` (`store_id`);

CREATE INDEX `i_volume_store_ref__volume_id`
  ON `volume_store_ref` (`volume_id`);

CREATE TABLE `volumes` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'Primary Key'
    PRIMARY KEY,
  `account_id` BIGINT NOT NULL
  COMMENT 'owner.  foreign key to account table',
  `domain_id` BIGINT NOT NULL
  COMMENT 'the domain that the owner belongs to',
  `pool_id` BIGINT NULL
  COMMENT 'pool it belongs to. foreign key to storage_pool table',
  `last_pool_id` BIGINT NULL
  COMMENT 'last pool it belongs to.',
  `instance_id` BIGINT NULL
  COMMENT 'vm instance it belongs to. foreign key to vm_instance table',
  `device_id` BIGINT NULL
  COMMENT 'which device inside vm instance it is',
  `name` VARCHAR(255) NULL
  COMMENT 'A user specified name for the volume',
  `uuid` VARCHAR(40) NULL,
  `size` BIGINT NOT NULL
  COMMENT 'total size',
  `folder` VARCHAR(255) NULL
  COMMENT 'The folder where the volume is saved',
  `path` VARCHAR(255) NULL
  COMMENT 'Path',
  `pod_id` BIGINT NULL
  COMMENT 'pod this volume belongs to',
  `data_center_id` BIGINT NOT NULL
  COMMENT 'data center this volume belongs to',
  `iscsi_name` VARCHAR(255) NULL
  COMMENT 'iscsi target name',
  `host_ip` CHAR(40) NULL
  COMMENT 'host ip address for convenience',
  `volume_type` VARCHAR(64) NOT NULL
  COMMENT 'root, swap or data',
  `pool_type` VARCHAR(64) NULL
  COMMENT 'type of the pool',
  `disk_offering_id` BIGINT NOT NULL
  COMMENT 'can be null for system VMs',
  `template_id` BIGINT NULL
  COMMENT 'fk to vm_template.id',
  `first_snapshot_backup_uuid` VARCHAR(255) NULL
  COMMENT 'The first snapshot that was ever taken for this volume',
  `recreatable` TINYINT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT 'Is this volume recreatable?',
  `created` DATETIME NULL
  COMMENT 'Date Created',
  `attached` DATETIME NULL
  COMMENT 'Date Attached',
  `updated` DATETIME NULL
  COMMENT 'Date updated for attach/detach',
  `removed` DATETIME NULL
  COMMENT 'Date removed.  not null if removed',
  `state` VARCHAR(32) NULL
  COMMENT 'State machine',
  `chain_info` TEXT NULL
  COMMENT 'save possible disk chain info in primary storage',
  `update_count` BIGINT DEFAULT '0' NOT NULL
  COMMENT 'date state was updated',
  `disk_type` VARCHAR(255) NULL,
  `vm_snapshot_chain_size` BIGINT NULL,
  `iso_id` BIGINT NULL
  COMMENT 'The id of the iso from which the volume was created',
  `display_volume` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'Should volume be displayed to the end user',
  `format` VARCHAR(255) NULL
  COMMENT 'volume format',
  `min_iops` BIGINT NULL
  COMMENT 'Minimum IOPS',
  `max_iops` BIGINT NULL
  COMMENT 'Maximum IOPS',
  `hv_ss_reserve` INT(32) UNSIGNED NULL
  COMMENT 'Hypervisor snapshot reserve space as a percent of a volume (for managed storage using Xen or VMware)',
  `provisioning_type` VARCHAR(32) DEFAULT 'thin' NOT NULL
  COMMENT 'pre allocation setting of the volume',
  CONSTRAINT `uc_volumes__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_volumes__pool_id`
  FOREIGN KEY (`pool_id`) REFERENCES `cloud`.`storage_pool` (`id`),
  CONSTRAINT `fk_volumes__instance_id`
  FOREIGN KEY (`instance_id`) REFERENCES `cloud`.`vm_instance` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_volumes__account_id`
  ON `volumes` (`account_id`);

CREATE INDEX `i_volumes__data_center_id`
  ON `volumes` (`data_center_id`);

CREATE INDEX `i_volumes__instance_id`
  ON `volumes` (`instance_id`);

CREATE INDEX `i_volumes__last_pool_id`
  ON `volumes` (`last_pool_id`);

CREATE INDEX `i_volumes__pod_id`
  ON `volumes` (`pod_id`);

CREATE INDEX `i_volumes__pool_id`
  ON `volumes` (`pool_id`);

CREATE INDEX `i_volumes__removed`
  ON `volumes` (`removed`);

CREATE INDEX `i_volumes__state`
  ON `volumes` (`state`);

CREATE INDEX `i_volumes__update_count`
  ON `volumes` (`update_count`);

ALTER TABLE `snapshot_schedule`
  ADD CONSTRAINT `fk__snapshot_schedule_volume_id`
FOREIGN KEY (`volume_id`) REFERENCES `cloud`.`volumes` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `volume_details`
  ADD CONSTRAINT `fk_volume_details__volume_id`
FOREIGN KEY (`volume_id`) REFERENCES `cloud`.`volumes` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `volume_host_ref`
  ADD CONSTRAINT `fk_volume_host_ref__volume_id`
FOREIGN KEY (`volume_id`) REFERENCES `cloud`.`volumes` (`id`);

ALTER TABLE `volume_store_ref`
  ADD CONSTRAINT `fk_volume_store_ref__volume_id`
FOREIGN KEY (`volume_id`) REFERENCES `cloud`.`volumes` (`id`);

CREATE TABLE `vpc` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NOT NULL,
  `name` VARCHAR(255) NULL
  COMMENT 'vpc name',
  `display_text` VARCHAR(255) NULL
  COMMENT 'vpc display text',
  `cidr` VARCHAR(18) NULL
  COMMENT 'vpc cidr',
  `vpc_offering_id` BIGINT NOT NULL
  COMMENT 'vpc offering id that this vpc is created from',
  `zone_id` BIGINT NOT NULL
  COMMENT 'the id of the zone this Vpc belongs to',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'state of the VP (can be Enabled and Disabled)',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain the vpc belongs to',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner of this vpc',
  `network_domain` VARCHAR(255) NULL
  COMMENT 'network domain',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `restart_required` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if restart is required for the VPC',
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the vpc can be displayed to the end user',
  `uses_distributed_router` TINYINT(1) DEFAULT '0' NULL,
  `region_level_vpc` TINYINT(1) DEFAULT '0' NULL,
  `redundant` TINYINT(1) DEFAULT '0' NULL,
  `source_nat_list` VARCHAR(255) NULL
  COMMENT 'List of CIDRS to source NAT on VPC',
  `syslog_server_list` VARCHAR(255) NULL
  COMMENT 'List of IP addresses to configure as syslog servers on VPC',
  CONSTRAINT `fk_vpc__zone_id`
  FOREIGN KEY (`zone_id`) REFERENCES `cloud`.`data_center` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_vpc__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_vpc__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vpc__account_id`
  ON `vpc` (`account_id`);

CREATE INDEX `i_vpc__domain_id`
  ON `vpc` (`domain_id`);

CREATE INDEX `i_vpc__vpc_offering_id`
  ON `vpc` (`vpc_offering_id`);

CREATE INDEX `i_vpc__zone_id`
  ON `vpc` (`zone_id`);

CREATE INDEX `i_vpc__removed`
  ON `vpc` (`removed`);

ALTER TABLE `domain_router`
  ADD CONSTRAINT `fk_domain_router__vpc_id`
FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`);

ALTER TABLE `firewall_rules`
  ADD CONSTRAINT `fk_firewall_rules__vpc_id`
FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `networks`
  ADD CONSTRAINT `fk_networks__vpc_id`
FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`);

ALTER TABLE `private_ip_address`
  ADD CONSTRAINT `fk_private_ip_address__vpc_id`
FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`);

ALTER TABLE `s2s_vpn_gateway`
  ADD CONSTRAINT `fk_s2s_vpn_gateway__vpc_id`
FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `static_routes`
  ADD CONSTRAINT `fk_static_routes__vpc_id`
FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`)
  ON DELETE CASCADE;

ALTER TABLE `user_ip_address`
  ADD CONSTRAINT `fk_user_ip_address__vpc_id`
FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `vpc_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vpc_id` BIGINT NOT NULL
  COMMENT 'VPC id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user',
  CONSTRAINT `fk_vpc_details__vpc_id`
  FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vpc_details__vpc_id`
  ON `vpc_details` (`vpc_id`);

CREATE TABLE `vpc_gateway_details` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vpc_gateway_id` BIGINT NOT NULL
  COMMENT 'VPC gateway id',
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(1024) NOT NULL,
  `display` TINYINT(1) DEFAULT '1' NOT NULL
  COMMENT 'True if the detail can be displayed to the end user'
);

CREATE INDEX `i_vpc_gateway_details__vpc_gateway_id`
  ON `vpc_gateway_details` (`vpc_gateway_id`);

CREATE TABLE `vpc_gateways` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `ip4_address` CHAR(40) NULL
  COMMENT 'ip4 address of the gateway',
  `type` VARCHAR(32) NULL
  COMMENT 'type of gateway; can be Public/Private/Vpn',
  `network_id` BIGINT NOT NULL
  COMMENT 'network id vpc gateway belongs to',
  `vpc_id` BIGINT NOT NULL
  COMMENT 'id of the vpc the gateway belongs to',
  `zone_id` BIGINT NOT NULL
  COMMENT 'id of the zone the gateway belongs to',
  `created` DATETIME NULL
  COMMENT 'date created',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner id',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'what state the vpc gateway in',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `source_nat` TINYINT(1) DEFAULT '0' NULL,
  `network_acl_id` BIGINT DEFAULT '1' NOT NULL,
  CONSTRAINT `uc_vpc_gateways__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `fk_vpc_gateways__network_id`
  FOREIGN KEY (`network_id`) REFERENCES `cloud`.`networks` (`id`),
  CONSTRAINT `fk_vpc_gateways__vpc_id`
  FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`),
  CONSTRAINT `fk_vpc_gateways__zone_id`
  FOREIGN KEY (`zone_id`) REFERENCES `cloud`.`data_center` (`id`),
  CONSTRAINT `fk_vpc_gateways__account_id`
  FOREIGN KEY (`account_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_vpc_gateways__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vpc_gateways__account_id`
  ON `vpc_gateways` (`account_id`);

CREATE INDEX `i_vpc_gateways__domain_id`
  ON `vpc_gateways` (`domain_id`);

CREATE INDEX `i_vpc_gateways__network_id`
  ON `vpc_gateways` (`network_id`);

CREATE INDEX `i_vpc_gateways__vpc_id`
  ON `vpc_gateways` (`vpc_id`);

CREATE INDEX `i_vpc_gateways__zone_id`
  ON `vpc_gateways` (`zone_id`);

CREATE INDEX `i_vpc_gateways__removed`
  ON `vpc_gateways` (`removed`);

ALTER TABLE `vpc_gateway_details`
  ADD CONSTRAINT `fk_vpc_gateway_details__vpc_gateway_id`
FOREIGN KEY (`vpc_gateway_id`) REFERENCES `cloud`.`vpc_gateways` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `vpc_gateways_pre530` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `ip4_address` CHAR(40) NULL
  COMMENT 'ip4 address of the gateway',
  `netmask` VARCHAR(15) NULL
  COMMENT 'netmask of the gateway',
  `gateway` VARCHAR(15) NULL
  COMMENT 'gateway',
  `vlan_tag` VARCHAR(255) NULL,
  `type` VARCHAR(32) NULL
  COMMENT 'type of gateway; can be Public/Private/Vpn',
  `network_id` BIGINT NOT NULL
  COMMENT 'network id vpc gateway belongs to',
  `vpc_id` BIGINT NOT NULL
  COMMENT 'id of the vpc the gateway belongs to',
  `zone_id` BIGINT NOT NULL
  COMMENT 'id of the zone the gateway belongs to',
  `created` DATETIME NULL
  COMMENT 'date created',
  `account_id` BIGINT NOT NULL
  COMMENT 'owner id',
  `domain_id` BIGINT NOT NULL
  COMMENT 'domain id',
  `state` VARCHAR(32) NOT NULL
  COMMENT 'what state the vpc gateway in',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `source_nat` TINYINT(1) DEFAULT '0' NULL,
  `network_acl_id` BIGINT DEFAULT '1' NOT NULL,
  CONSTRAINT `uc_vpc_gateways__uuid`
  UNIQUE (`uuid`)
);

CREATE INDEX `i_vpc_gateways__account_id`
  ON `vpc_gateways_pre530` (`account_id`);

CREATE INDEX `i_vpc_gateways__domain_id`
  ON `vpc_gateways_pre530` (`domain_id`);

CREATE INDEX `i_vpc_gateways__network_id`
  ON `vpc_gateways_pre530` (`network_id`);

CREATE INDEX `i_vpc_gateways__vpc_id`
  ON `vpc_gateways_pre530` (`vpc_id`);

CREATE INDEX `i_vpc_gateways__zone_id`
  ON `vpc_gateways_pre530` (`zone_id`);

CREATE INDEX `i_vpc_gateways__removed`
  ON `vpc_gateways_pre530` (`removed`);

CREATE TABLE `vpc_offering_service_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vpc_offering_id` BIGINT NOT NULL
  COMMENT 'vpc_offering_id',
  `service` VARCHAR(255) NOT NULL
  COMMENT 'service',
  `provider` VARCHAR(255) NULL
  COMMENT 'service provider',
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `vpc_offering_id`
  UNIQUE (`vpc_offering_id`, `service`, `provider`)
);

CREATE TABLE `vpc_offerings` (
  `id` BIGINT AUTO_INCREMENT
  COMMENT 'id'
    PRIMARY KEY,
  `uuid` VARCHAR(40) NOT NULL,
  `unique_name` VARCHAR(64) NULL
  COMMENT 'unique name of the vpc offering',
  `name` VARCHAR(255) NULL
  COMMENT 'vpc name',
  `display_text` VARCHAR(255) NULL
  COMMENT 'display text',
  `state` CHAR(32) NULL
  COMMENT 'state of the vpc offering that has Disabled value by default',
  `default` INT(1) UNSIGNED DEFAULT '0' NOT NULL
  COMMENT '1 if vpc offering is default',
  `removed` DATETIME NULL
  COMMENT 'date removed if not null',
  `created` DATETIME NOT NULL
  COMMENT 'date created',
  `service_offering_id` BIGINT NULL
  COMMENT 'service offering id that virtual router is tied to',
  `supports_distributed_router` TINYINT(1) DEFAULT '0' NULL,
  `supports_region_level_vpc` TINYINT(1) DEFAULT '0' NULL,
  `redundant_router_service` TINYINT(1) DEFAULT '0' NULL,
  `secondary_service_offering_id` BIGINT NULL
  COMMENT 'service offering id that a secondary virtual router is tied to',
  CONSTRAINT `unique_name`
  UNIQUE (`unique_name`),
  CONSTRAINT `fk_vpc_offerings__service_offering_id`
  FOREIGN KEY (`service_offering_id`) REFERENCES `cloud`.`service_offering` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vpc_offerings__service_offering_id`
  ON `vpc_offerings` (`service_offering_id`);

CREATE INDEX `i_vpc__removed`
  ON `vpc_offerings` (`removed`);

ALTER TABLE `vpc`
  ADD CONSTRAINT `fk_vpc__vpc_offering_id`
FOREIGN KEY (`vpc_offering_id`) REFERENCES `cloud`.`vpc_offerings` (`id`);

ALTER TABLE `vpc_offering_service_map`
  ADD CONSTRAINT `fk_vpc_offering_service_map__vpc_offering_id`
FOREIGN KEY (`vpc_offering_id`) REFERENCES `cloud`.`vpc_offerings` (`id`)
  ON DELETE CASCADE;

CREATE TABLE `vpc_service_map` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `vpc_id` BIGINT NOT NULL
  COMMENT 'vpc_id',
  `service` VARCHAR(255) NOT NULL
  COMMENT 'service',
  `provider` VARCHAR(255) NULL
  COMMENT 'service provider',
  `created` DATETIME NULL
  COMMENT 'date created',
  CONSTRAINT `vpc_id`
  UNIQUE (`vpc_id`, `service`, `provider`),
  CONSTRAINT `fk_vpc_service_map__vpc_id`
  FOREIGN KEY (`vpc_id`) REFERENCES `cloud`.`vpc` (`id`)
    ON DELETE CASCADE
);

CREATE TABLE `vpn_users` (
  `id` BIGINT AUTO_INCREMENT
    PRIMARY KEY,
  `uuid` VARCHAR(40) NULL,
  `owner_id` BIGINT NOT NULL,
  `domain_id` BIGINT NOT NULL,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `state` CHAR(32) NOT NULL
  COMMENT 'What state is this vpn user in',
  CONSTRAINT `uc_vpn_users__uuid`
  UNIQUE (`uuid`),
  CONSTRAINT `i_vpn_users__account_id__username`
  UNIQUE (`owner_id`, `username`),
  CONSTRAINT `fk_vpn_users__owner_id`
  FOREIGN KEY (`owner_id`) REFERENCES `cloud`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_vpn_users__domain_id`
  FOREIGN KEY (`domain_id`) REFERENCES `cloud`.`domain` (`id`)
    ON DELETE CASCADE
);

CREATE INDEX `i_vpn_users__domain_id`
  ON `vpn_users` (`domain_id`);

CREATE INDEX `i_vpn_users_username`
  ON `vpn_users` (`username`);

CREATE VIEW `account_netstats_view` AS
  SELECT
    `cloud`.`user_statistics`.`account_id` AS `account_id`,
    (sum(`cloud`.`user_statistics`.`net_bytes_received`) +
     sum(`cloud`.`user_statistics`.`current_bytes_received`)) AS `bytesReceived`,
    (sum(`cloud`.`user_statistics`.`net_bytes_sent`) +
     sum(`cloud`.`user_statistics`.`current_bytes_sent`)) AS `bytesSent`
  FROM `cloud`.`user_statistics`
  GROUP BY `cloud`.`user_statistics`.`account_id`;

CREATE VIEW `free_ip_view` AS
  SELECT count(`cloud`.`user_ip_address`.`id`) AS `free_ip`
  FROM (`cloud`.`user_ip_address`
    JOIN `cloud`.`vlan` ON (((`cloud`.`vlan`.`id` = `cloud`.`user_ip_address`.`vlan_db_id`) AND
                             (`cloud`.`vlan`.`vlan_type` = 'VirtualNetwork'))))
  WHERE (`cloud`.`user_ip_address`.`state` = 'Free');

CREATE VIEW `account_vmstats_view` AS
  SELECT
    `cloud`.`vm_instance`.`account_id` AS `account_id`,
    `cloud`.`vm_instance`.`state` AS `state`,
    count(0) AS `vmcount`
  FROM `cloud`.`vm_instance`
  WHERE ((`cloud`.`vm_instance`.`vm_type` = 'User') AND isnull(`cloud`.`vm_instance`.`removed`))
  GROUP BY `cloud`.`vm_instance`.`account_id`, `cloud`.`vm_instance`.`state`;

CREATE VIEW `account_view` AS
  SELECT
    `cloud`.`account`.`id` AS `id`,
    `cloud`.`account`.`uuid` AS `uuid`,
    `cloud`.`account`.`account_name` AS `account_name`,
    `cloud`.`account`.`type` AS `type`,
    `cloud`.`account`.`state` AS `state`,
    `cloud`.`account`.`removed` AS `removed`,
    `cloud`.`account`.`cleanup_needed` AS `cleanup_needed`,
    `cloud`.`account`.`network_domain` AS `network_domain`,
    `cloud`.`account`.`default` AS `default`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`,
    `cloud`.`data_center`.`id` AS `data_center_id`,
    `cloud`.`data_center`.`uuid` AS `data_center_uuid`,
    `cloud`.`data_center`.`name` AS `data_center_name`,
    `account_netstats_view`.`bytesReceived` AS `bytesReceived`,
    `account_netstats_view`.`bytesSent` AS `bytesSent`,
    `vmlimit`.`max` AS `vmLimit`,
    `vmcount`.`count` AS `vmTotal`,
    `runningvm`.`vmcount` AS `runningVms`,
    `stoppedvm`.`vmcount` AS `stoppedVms`,
    `iplimit`.`max` AS `ipLimit`,
    `ipcount`.`count` AS `ipTotal`,
    `cloud`.`free_ip_view`.`free_ip` AS `ipFree`,
    `volumelimit`.`max` AS `volumeLimit`,
    `volumecount`.`count` AS `volumeTotal`,
    `snapshotlimit`.`max` AS `snapshotLimit`,
    `snapshotcount`.`count` AS `snapshotTotal`,
    `templatelimit`.`max` AS `templateLimit`,
    `templatecount`.`count` AS `templateTotal`,
    `vpclimit`.`max` AS `vpcLimit`,
    `vpccount`.`count` AS `vpcTotal`,
    `projectlimit`.`max` AS `projectLimit`,
    `projectcount`.`count` AS `projectTotal`,
    `networklimit`.`max` AS `networkLimit`,
    `networkcount`.`count` AS `networkTotal`,
    `cpulimit`.`max` AS `cpuLimit`,
    `cpucount`.`count` AS `cpuTotal`,
    `memorylimit`.`max` AS `memoryLimit`,
    `memorycount`.`count` AS `memoryTotal`,
    `primary_storage_limit`.`max` AS `primaryStorageLimit`,
    `primary_storage_count`.`count` AS `primaryStorageTotal`,
    `secondary_storage_limit`.`max` AS `secondaryStorageLimit`,
    `secondary_storage_count`.`count` AS `secondaryStorageTotal`,
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`
  FROM (`cloud`.`free_ip_view`
    JOIN ((((((((((((((((((((((((((((((`cloud`.`account`
      JOIN `cloud`.`domain` ON ((`cloud`.`account`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN
      `cloud`.`data_center` ON ((`cloud`.`account`.`default_zone_id` = `cloud`.`data_center`.`id`))) LEFT JOIN
      `cloud`.`account_netstats_view` ON ((`cloud`.`account`.`id` = `account_netstats_view`.`account_id`))) LEFT JOIN
      `cloud`.`resource_limit` `vmlimit`
        ON (((`cloud`.`account`.`id` = `vmlimit`.`account_id`) AND (`vmlimit`.`type` = 'user_vm')))) LEFT JOIN
      `cloud`.`resource_count` `vmcount`
        ON (((`cloud`.`account`.`id` = `vmcount`.`account_id`) AND (`vmcount`.`type` = 'user_vm')))) LEFT JOIN
      `cloud`.`account_vmstats_view` `runningvm`
        ON (((`cloud`.`account`.`id` = `runningvm`.`account_id`) AND (`runningvm`.`state` = 'Running')))) LEFT JOIN
      `cloud`.`account_vmstats_view` `stoppedvm`
        ON (((`cloud`.`account`.`id` = `stoppedvm`.`account_id`) AND (`stoppedvm`.`state` = 'Stopped')))) LEFT JOIN
      `cloud`.`resource_limit` `iplimit`
        ON (((`cloud`.`account`.`id` = `iplimit`.`account_id`) AND (`iplimit`.`type` = 'public_ip')))) LEFT JOIN
      `cloud`.`resource_count` `ipcount`
        ON (((`cloud`.`account`.`id` = `ipcount`.`account_id`) AND (`ipcount`.`type` = 'public_ip')))) LEFT JOIN
      `cloud`.`resource_limit` `volumelimit`
        ON (((`cloud`.`account`.`id` = `volumelimit`.`account_id`) AND (`volumelimit`.`type` = 'volume')))) LEFT JOIN
      `cloud`.`resource_count` `volumecount`
        ON (((`cloud`.`account`.`id` = `volumecount`.`account_id`) AND (`volumecount`.`type` = 'volume')))) LEFT JOIN
      `cloud`.`resource_limit` `snapshotlimit` ON (((`cloud`.`account`.`id` = `snapshotlimit`.`account_id`) AND
                                                    (`snapshotlimit`.`type` = 'snapshot')))) LEFT JOIN
      `cloud`.`resource_count` `snapshotcount` ON (((`cloud`.`account`.`id` = `snapshotcount`.`account_id`) AND
                                                    (`snapshotcount`.`type` = 'snapshot')))) LEFT JOIN
      `cloud`.`resource_limit` `templatelimit` ON (((`cloud`.`account`.`id` = `templatelimit`.`account_id`) AND
                                                    (`templatelimit`.`type` = 'template')))) LEFT JOIN
      `cloud`.`resource_count` `templatecount` ON (((`cloud`.`account`.`id` = `templatecount`.`account_id`) AND
                                                    (`templatecount`.`type` = 'template')))) LEFT JOIN
      `cloud`.`resource_limit` `vpclimit`
        ON (((`cloud`.`account`.`id` = `vpclimit`.`account_id`) AND (`vpclimit`.`type` = 'vpc')))) LEFT JOIN
      `cloud`.`resource_count` `vpccount`
        ON (((`cloud`.`account`.`id` = `vpccount`.`account_id`) AND (`vpccount`.`type` = 'vpc')))) LEFT JOIN
      `cloud`.`resource_limit` `projectlimit`
        ON (((`cloud`.`account`.`id` = `projectlimit`.`account_id`) AND (`projectlimit`.`type` = 'project')))) LEFT JOIN
      `cloud`.`resource_count` `projectcount`
        ON (((`cloud`.`account`.`id` = `projectcount`.`account_id`) AND (`projectcount`.`type` = 'project')))) LEFT JOIN
      `cloud`.`resource_limit` `networklimit`
        ON (((`cloud`.`account`.`id` = `networklimit`.`account_id`) AND (`networklimit`.`type` = 'network')))) LEFT JOIN
      `cloud`.`resource_count` `networkcount`
        ON (((`cloud`.`account`.`id` = `networkcount`.`account_id`) AND (`networkcount`.`type` = 'network')))) LEFT JOIN
      `cloud`.`resource_limit` `cpulimit`
        ON (((`cloud`.`account`.`id` = `cpulimit`.`account_id`) AND (`cpulimit`.`type` = 'cpu')))) LEFT JOIN
      `cloud`.`resource_count` `cpucount`
        ON (((`cloud`.`account`.`id` = `cpucount`.`account_id`) AND (`cpucount`.`type` = 'cpu')))) LEFT JOIN
      `cloud`.`resource_limit` `memorylimit`
        ON (((`cloud`.`account`.`id` = `memorylimit`.`account_id`) AND (`memorylimit`.`type` = 'memory')))) LEFT JOIN
      `cloud`.`resource_count` `memorycount`
        ON (((`cloud`.`account`.`id` = `memorycount`.`account_id`) AND (`memorycount`.`type` = 'memory')))) LEFT JOIN
      `cloud`.`resource_limit` `primary_storage_limit`
        ON (((`cloud`.`account`.`id` = `primary_storage_limit`.`account_id`) AND
             (`primary_storage_limit`.`type` = 'primary_storage')))) LEFT JOIN
      `cloud`.`resource_count` `primary_storage_count`
        ON (((`cloud`.`account`.`id` = `primary_storage_count`.`account_id`) AND
             (`primary_storage_count`.`type` = 'primary_storage')))) LEFT JOIN
      `cloud`.`resource_limit` `secondary_storage_limit`
        ON (((`cloud`.`account`.`id` = `secondary_storage_limit`.`account_id`) AND
             (`secondary_storage_limit`.`type` = 'secondary_storage')))) LEFT JOIN
      `cloud`.`resource_count` `secondary_storage_count`
        ON (((`cloud`.`account`.`id` = `secondary_storage_count`.`account_id`) AND
             (`secondary_storage_count`.`type` = 'secondary_storage')))) LEFT JOIN `cloud`.`async_job` ON ((
      (`cloud`.`async_job`.`instance_id` = `cloud`.`account`.`id`) AND (`cloud`.`async_job`.`instance_type` = 'Account')
      AND (`cloud`.`async_job`.`job_status` = 0)))));

CREATE VIEW `affinity_group_view` AS
  SELECT
    `cloud`.`affinity_group`.`id` AS `id`,
    `cloud`.`affinity_group`.`name` AS `name`,
    `cloud`.`affinity_group`.`type` AS `type`,
    `cloud`.`affinity_group`.`description` AS `description`,
    `cloud`.`affinity_group`.`uuid` AS `uuid`,
    `cloud`.`affinity_group`.`acl_type` AS `acl_type`,
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
    `cloud`.`vm_instance`.`id` AS `vm_id`,
    `cloud`.`vm_instance`.`uuid` AS `vm_uuid`,
    `cloud`.`vm_instance`.`name` AS `vm_name`,
    `cloud`.`vm_instance`.`state` AS `vm_state`,
    `cloud`.`user_vm`.`display_name` AS `vm_display_name`
  FROM ((((((`cloud`.`affinity_group`
    JOIN `cloud`.`account` ON ((`cloud`.`affinity_group`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`affinity_group`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`account`.`id`))) LEFT JOIN `cloud`.`affinity_group_vm_map`
      ON ((`cloud`.`affinity_group`.`id` = `cloud`.`affinity_group_vm_map`.`affinity_group_id`))) LEFT JOIN
    `cloud`.`vm_instance` ON ((`cloud`.`vm_instance`.`id` = `cloud`.`affinity_group_vm_map`.`instance_id`))) LEFT JOIN
    `cloud`.`user_vm` ON ((`cloud`.`user_vm`.`id` = `cloud`.`vm_instance`.`id`)));

CREATE VIEW `async_job_view` AS
  SELECT
    `cloud`.`account`.`id` AS `account_id`,
    `cloud`.`account`.`uuid` AS `account_uuid`,
    `cloud`.`account`.`account_name` AS `account_name`,
    `cloud`.`account`.`type` AS `account_type`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`,
    `cloud`.`user`.`id` AS `user_id`,
    `cloud`.`user`.`uuid` AS `user_uuid`,
    `cloud`.`async_job`.`id` AS `id`,
    `cloud`.`async_job`.`uuid` AS `uuid`,
    `cloud`.`async_job`.`job_cmd` AS `job_cmd`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`job_process_status` AS `job_process_status`,
    `cloud`.`async_job`.`job_result_code` AS `job_result_code`,
    `cloud`.`async_job`.`job_result` AS `job_result`,
    `cloud`.`async_job`.`created` AS `created`,
    `cloud`.`async_job`.`removed` AS `removed`,
    `cloud`.`async_job`.`instance_type` AS `instance_type`,
    `cloud`.`async_job`.`instance_id` AS `instance_id`,
    (CASE WHEN (`cloud`.`async_job`.`instance_type` = 'Volume')
      THEN `cloud`.`volumes`.`uuid`
     WHEN ((`cloud`.`async_job`.`instance_type` = 'Template') OR (`cloud`.`async_job`.`instance_type` = 'Iso'))
       THEN `cloud`.`vm_template`.`uuid`
     WHEN ((`cloud`.`async_job`.`instance_type` = 'VirtualMachine') OR
           (`cloud`.`async_job`.`instance_type` = 'ConsoleProxy') OR (`cloud`.`async_job`.`instance_type` = 'SystemVm')
           OR (`cloud`.`async_job`.`instance_type` = 'DomainRouter'))
       THEN `cloud`.`vm_instance`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'Snapshot')
       THEN `cloud`.`snapshots`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'Host')
       THEN `cloud`.`host`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'StoragePool')
       THEN `cloud`.`storage_pool`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'IpAddress')
       THEN `cloud`.`user_ip_address`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'SecurityGroup')
       THEN `cloud`.`security_group`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'PhysicalNetwork')
       THEN `cloud`.`physical_network`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'TrafficType')
       THEN `cloud`.`physical_network_traffic_types`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'PhysicalNetworkServiceProvider')
       THEN `cloud`.`physical_network_service_providers`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'FirewallRule')
       THEN `cloud`.`firewall_rules`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'Account')
       THEN `acct`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'User')
       THEN `us`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'StaticRoute')
       THEN `cloud`.`static_routes`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'PrivateGateway')
       THEN `cloud`.`vpc_gateways`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'Counter')
       THEN `cloud`.`counter`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'Condition')
       THEN `cloud`.`conditions`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'AutoScalePolicy')
       THEN `cloud`.`autoscale_policies`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'AutoScaleVmProfile')
       THEN `cloud`.`autoscale_vmprofiles`.`uuid`
     WHEN (`cloud`.`async_job`.`instance_type` = 'AutoScaleVmGroup')
       THEN `cloud`.`autoscale_vmgroups`.`uuid`
     ELSE NULL END) AS `instance_uuid`
  FROM ((((((((((((((((((((((((`cloud`.`async_job`
    LEFT JOIN `cloud`.`account` ON ((`cloud`.`async_job`.`account_id` = `cloud`.`account`.`id`))) LEFT JOIN
    `cloud`.`domain` ON ((`cloud`.`domain`.`id` = `cloud`.`account`.`domain_id`))) LEFT JOIN `cloud`.`user`
      ON ((`cloud`.`async_job`.`user_id` = `cloud`.`user`.`id`))) LEFT JOIN `cloud`.`volumes`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`volumes`.`id`))) LEFT JOIN `cloud`.`vm_template`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN `cloud`.`vm_instance`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`vm_instance`.`id`))) LEFT JOIN `cloud`.`snapshots`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`snapshots`.`id`))) LEFT JOIN `cloud`.`host`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`host`.`id`))) LEFT JOIN `cloud`.`storage_pool`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`storage_pool`.`id`))) LEFT JOIN `cloud`.`user_ip_address`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`user_ip_address`.`id`))) LEFT JOIN `cloud`.`security_group`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`security_group`.`id`))) LEFT JOIN `cloud`.`physical_network`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`physical_network`.`id`))) LEFT JOIN
    `cloud`.`physical_network_traffic_types`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`physical_network_traffic_types`.`id`))) LEFT JOIN
    `cloud`.`physical_network_service_providers`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`physical_network_service_providers`.`id`))) LEFT JOIN
    `cloud`.`firewall_rules` ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`firewall_rules`.`id`))) LEFT JOIN
    `cloud`.`account` `acct` ON ((`cloud`.`async_job`.`instance_id` = `acct`.`id`))) LEFT JOIN `cloud`.`user` `us`
      ON ((`cloud`.`async_job`.`instance_id` = `us`.`id`))) LEFT JOIN `cloud`.`static_routes`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`static_routes`.`id`))) LEFT JOIN `cloud`.`vpc_gateways`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`vpc_gateways`.`id`))) LEFT JOIN `cloud`.`counter`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`counter`.`id`))) LEFT JOIN `cloud`.`conditions`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`conditions`.`id`))) LEFT JOIN `cloud`.`autoscale_policies`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`autoscale_policies`.`id`))) LEFT JOIN
    `cloud`.`autoscale_vmprofiles`
      ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`autoscale_vmprofiles`.`id`))) LEFT JOIN
    `cloud`.`autoscale_vmgroups` ON ((`cloud`.`async_job`.`instance_id` = `cloud`.`autoscale_vmgroups`.`id`)));

CREATE VIEW `data_center_view` AS
  SELECT
    `cloud`.`data_center`.`id` AS `id`,
    `cloud`.`data_center`.`uuid` AS `uuid`,
    `cloud`.`data_center`.`name` AS `name`,
    `cloud`.`data_center`.`is_security_group_enabled` AS `is_security_group_enabled`,
    `cloud`.`data_center`.`is_local_storage_enabled` AS `is_local_storage_enabled`,
    `cloud`.`data_center`.`description` AS `description`,
    `cloud`.`data_center`.`dns1` AS `dns1`,
    `cloud`.`data_center`.`dns2` AS `dns2`,
    `cloud`.`data_center`.`ip6_dns1` AS `ip6_dns1`,
    `cloud`.`data_center`.`ip6_dns2` AS `ip6_dns2`,
    `cloud`.`data_center`.`internal_dns1` AS `internal_dns1`,
    `cloud`.`data_center`.`internal_dns2` AS `internal_dns2`,
    `cloud`.`data_center`.`guest_network_cidr` AS `guest_network_cidr`,
    `cloud`.`data_center`.`domain` AS `domain`,
    `cloud`.`data_center`.`networktype` AS `networktype`,
    `cloud`.`data_center`.`allocation_state` AS `allocation_state`,
    `cloud`.`data_center`.`zone_token` AS `zone_token`,
    `cloud`.`data_center`.`dhcp_provider` AS `dhcp_provider`,
    `cloud`.`data_center`.`removed` AS `removed`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`,
    `cloud`.`dedicated_resources`.`affinity_group_id` AS `affinity_group_id`,
    `cloud`.`dedicated_resources`.`account_id` AS `account_id`,
    `cloud`.`affinity_group`.`uuid` AS `affinity_group_uuid`
  FROM (((`cloud`.`data_center`
    LEFT JOIN `cloud`.`domain` ON ((`cloud`.`data_center`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN
    `cloud`.`dedicated_resources`
      ON ((`cloud`.`data_center`.`id` = `cloud`.`dedicated_resources`.`data_center_id`))) LEFT JOIN
    `cloud`.`affinity_group` ON ((`cloud`.`dedicated_resources`.`affinity_group_id` = `cloud`.`affinity_group`.`id`)));

CREATE VIEW `disk_offering_view` AS
  SELECT
    `cloud`.`disk_offering`.`id` AS `id`,
    `cloud`.`disk_offering`.`uuid` AS `uuid`,
    `cloud`.`disk_offering`.`name` AS `name`,
    `cloud`.`disk_offering`.`display_text` AS `display_text`,
    `cloud`.`disk_offering`.`provisioning_type` AS `provisioning_type`,
    `cloud`.`disk_offering`.`disk_size` AS `disk_size`,
    `cloud`.`disk_offering`.`min_iops` AS `min_iops`,
    `cloud`.`disk_offering`.`max_iops` AS `max_iops`,
    `cloud`.`disk_offering`.`created` AS `created`,
    `cloud`.`disk_offering`.`tags` AS `tags`,
    `cloud`.`disk_offering`.`customized` AS `customized`,
    `cloud`.`disk_offering`.`customized_iops` AS `customized_iops`,
    `cloud`.`disk_offering`.`removed` AS `removed`,
    `cloud`.`disk_offering`.`use_local_storage` AS `use_local_storage`,
    `cloud`.`disk_offering`.`system_use` AS `system_use`,
    `cloud`.`disk_offering`.`hv_ss_reserve` AS `hv_ss_reserve`,
    `cloud`.`disk_offering`.`bytes_read_rate` AS `bytes_read_rate`,
    `cloud`.`disk_offering`.`bytes_write_rate` AS `bytes_write_rate`,
    `cloud`.`disk_offering`.`iops_read_rate` AS `iops_read_rate`,
    `cloud`.`disk_offering`.`iops_write_rate` AS `iops_write_rate`,
    `cloud`.`disk_offering`.`cache_mode` AS `cache_mode`,
    `cloud`.`disk_offering`.`sort_key` AS `sort_key`,
    `cloud`.`disk_offering`.`type` AS `type`,
    `cloud`.`disk_offering`.`display_offering` AS `display_offering`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`
  FROM (`cloud`.`disk_offering`
    LEFT JOIN `cloud`.`domain` ON ((`cloud`.`disk_offering`.`domain_id` = `cloud`.`domain`.`id`)))
  WHERE (`cloud`.`disk_offering`.`state` = 'ACTIVE');

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

CREATE VIEW `domain_view` AS
  SELECT
    `cloud`.`domain`.`id` AS `id`,
    `cloud`.`domain`.`parent` AS `parent`,
    `cloud`.`domain`.`name` AS `name`,
    `cloud`.`domain`.`uuid` AS `uuid`,
    `cloud`.`domain`.`owner` AS `owner`,
    `cloud`.`domain`.`path` AS `path`,
    `cloud`.`domain`.`level` AS `level`,
    `cloud`.`domain`.`child_count` AS `child_count`,
    `cloud`.`domain`.`next_child_seq` AS `next_child_seq`,
    `cloud`.`domain`.`removed` AS `removed`,
    `cloud`.`domain`.`state` AS `state`,
    `cloud`.`domain`.`network_domain` AS `network_domain`,
    `cloud`.`domain`.`type` AS `type`,
    `cloud`.`domain`.`email` AS `email`,
    `vmlimit`.`max` AS `vmLimit`,
    `vmcount`.`count` AS `vmTotal`,
    `iplimit`.`max` AS `ipLimit`,
    `ipcount`.`count` AS `ipTotal`,
    `volumelimit`.`max` AS `volumeLimit`,
    `volumecount`.`count` AS `volumeTotal`,
    `snapshotlimit`.`max` AS `snapshotLimit`,
    `snapshotcount`.`count` AS `snapshotTotal`,
    `templatelimit`.`max` AS `templateLimit`,
    `templatecount`.`count` AS `templateTotal`,
    `vpclimit`.`max` AS `vpcLimit`,
    `vpccount`.`count` AS `vpcTotal`,
    `projectlimit`.`max` AS `projectLimit`,
    `projectcount`.`count` AS `projectTotal`,
    `networklimit`.`max` AS `networkLimit`,
    `networkcount`.`count` AS `networkTotal`,
    `cpulimit`.`max` AS `cpuLimit`,
    `cpucount`.`count` AS `cpuTotal`,
    `memorylimit`.`max` AS `memoryLimit`,
    `memorycount`.`count` AS `memoryTotal`,
    `primary_storage_limit`.`max` AS `primaryStorageLimit`,
    `primary_storage_count`.`count` AS `primaryStorageTotal`,
    `secondary_storage_limit`.`max` AS `secondaryStorageLimit`,
    `secondary_storage_count`.`count` AS `secondaryStorageTotal`
  FROM ((((((((((((((((((((((((`cloud`.`domain`
    LEFT JOIN `cloud`.`resource_limit` `vmlimit`
      ON (((`cloud`.`domain`.`id` = `vmlimit`.`domain_id`) AND (`vmlimit`.`type` = 'user_vm')))) LEFT JOIN
    `cloud`.`resource_count` `vmcount`
      ON (((`cloud`.`domain`.`id` = `vmcount`.`domain_id`) AND (`vmcount`.`type` = 'user_vm')))) LEFT JOIN
    `cloud`.`resource_limit` `iplimit`
      ON (((`cloud`.`domain`.`id` = `iplimit`.`domain_id`) AND (`iplimit`.`type` = 'public_ip')))) LEFT JOIN
    `cloud`.`resource_count` `ipcount`
      ON (((`cloud`.`domain`.`id` = `ipcount`.`domain_id`) AND (`ipcount`.`type` = 'public_ip')))) LEFT JOIN
    `cloud`.`resource_limit` `volumelimit`
      ON (((`cloud`.`domain`.`id` = `volumelimit`.`domain_id`) AND (`volumelimit`.`type` = 'volume')))) LEFT JOIN
    `cloud`.`resource_count` `volumecount`
      ON (((`cloud`.`domain`.`id` = `volumecount`.`domain_id`) AND (`volumecount`.`type` = 'volume')))) LEFT JOIN
    `cloud`.`resource_limit` `snapshotlimit`
      ON (((`cloud`.`domain`.`id` = `snapshotlimit`.`domain_id`) AND (`snapshotlimit`.`type` = 'snapshot')))) LEFT JOIN
    `cloud`.`resource_count` `snapshotcount`
      ON (((`cloud`.`domain`.`id` = `snapshotcount`.`domain_id`) AND (`snapshotcount`.`type` = 'snapshot')))) LEFT JOIN
    `cloud`.`resource_limit` `templatelimit`
      ON (((`cloud`.`domain`.`id` = `templatelimit`.`domain_id`) AND (`templatelimit`.`type` = 'template')))) LEFT JOIN
    `cloud`.`resource_count` `templatecount`
      ON (((`cloud`.`domain`.`id` = `templatecount`.`domain_id`) AND (`templatecount`.`type` = 'template')))) LEFT JOIN
    `cloud`.`resource_limit` `vpclimit`
      ON (((`cloud`.`domain`.`id` = `vpclimit`.`domain_id`) AND (`vpclimit`.`type` = 'vpc')))) LEFT JOIN
    `cloud`.`resource_count` `vpccount`
      ON (((`cloud`.`domain`.`id` = `vpccount`.`domain_id`) AND (`vpccount`.`type` = 'vpc')))) LEFT JOIN
    `cloud`.`resource_limit` `projectlimit`
      ON (((`cloud`.`domain`.`id` = `projectlimit`.`domain_id`) AND (`projectlimit`.`type` = 'project')))) LEFT JOIN
    `cloud`.`resource_count` `projectcount`
      ON (((`cloud`.`domain`.`id` = `projectcount`.`domain_id`) AND (`projectcount`.`type` = 'project')))) LEFT JOIN
    `cloud`.`resource_limit` `networklimit`
      ON (((`cloud`.`domain`.`id` = `networklimit`.`domain_id`) AND (`networklimit`.`type` = 'network')))) LEFT JOIN
    `cloud`.`resource_count` `networkcount`
      ON (((`cloud`.`domain`.`id` = `networkcount`.`domain_id`) AND (`networkcount`.`type` = 'network')))) LEFT JOIN
    `cloud`.`resource_limit` `cpulimit`
      ON (((`cloud`.`domain`.`id` = `cpulimit`.`domain_id`) AND (`cpulimit`.`type` = 'cpu')))) LEFT JOIN
    `cloud`.`resource_count` `cpucount`
      ON (((`cloud`.`domain`.`id` = `cpucount`.`domain_id`) AND (`cpucount`.`type` = 'cpu')))) LEFT JOIN
    `cloud`.`resource_limit` `memorylimit`
      ON (((`cloud`.`domain`.`id` = `memorylimit`.`domain_id`) AND (`memorylimit`.`type` = 'memory')))) LEFT JOIN
    `cloud`.`resource_count` `memorycount`
      ON (((`cloud`.`domain`.`id` = `memorycount`.`domain_id`) AND (`memorycount`.`type` = 'memory')))) LEFT JOIN
    `cloud`.`resource_limit` `primary_storage_limit`
      ON (((`cloud`.`domain`.`id` = `primary_storage_limit`.`domain_id`) AND
           (`primary_storage_limit`.`type` = 'primary_storage')))) LEFT JOIN
    `cloud`.`resource_count` `primary_storage_count`
      ON (((`cloud`.`domain`.`id` = `primary_storage_count`.`domain_id`) AND
           (`primary_storage_count`.`type` = 'primary_storage')))) LEFT JOIN
    `cloud`.`resource_limit` `secondary_storage_limit`
      ON (((`cloud`.`domain`.`id` = `secondary_storage_limit`.`domain_id`) AND
           (`secondary_storage_limit`.`type` = 'secondary_storage')))) LEFT JOIN
    `cloud`.`resource_count` `secondary_storage_count`
      ON (((`cloud`.`domain`.`id` = `secondary_storage_count`.`domain_id`) AND
           (`secondary_storage_count`.`type` = 'secondary_storage'))));

CREATE VIEW `event_view` AS
  SELECT
    `cloud`.`event`.`id` AS `id`,
    `cloud`.`event`.`uuid` AS `uuid`,
    `cloud`.`event`.`type` AS `type`,
    `cloud`.`event`.`state` AS `state`,
    `cloud`.`event`.`description` AS `description`,
    `cloud`.`event`.`created` AS `created`,
    `cloud`.`event`.`level` AS `level`,
    `cloud`.`event`.`parameters` AS `parameters`,
    `cloud`.`event`.`start_id` AS `start_id`,
    `eve`.`uuid` AS `start_uuid`,
    `cloud`.`event`.`user_id` AS `user_id`,
    `cloud`.`event`.`archived` AS `archived`,
    `cloud`.`event`.`display` AS `display`,
    `cloud`.`user`.`username` AS `user_name`,
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
    `cloud`.`projects`.`name` AS `project_name`
  FROM (((((`cloud`.`event`
    JOIN `cloud`.`account` ON ((`cloud`.`event`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`event`.`domain_id` = `cloud`.`domain`.`id`))) JOIN `cloud`.`user`
      ON ((`cloud`.`event`.`user_id` = `cloud`.`user`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`event`.`account_id`))) LEFT JOIN `cloud`.`event` `eve`
      ON ((`cloud`.`event`.`start_id` = `eve`.`id`)));

CREATE VIEW `host_view` AS
  SELECT
    `cloud`.`host`.`id` AS `id`,
    `cloud`.`host`.`uuid` AS `uuid`,
    `cloud`.`host`.`name` AS `name`,
    `cloud`.`host`.`status` AS `status`,
    `cloud`.`host`.`disconnected` AS `disconnected`,
    `cloud`.`host`.`type` AS `type`,
    `cloud`.`host`.`private_ip_address` AS `private_ip_address`,
    `cloud`.`host`.`version` AS `version`,
    `cloud`.`host`.`hypervisor_type` AS `hypervisor_type`,
    `cloud`.`host`.`hypervisor_version` AS `hypervisor_version`,
    `cloud`.`host`.`capabilities` AS `capabilities`,
    `cloud`.`host`.`last_ping` AS `last_ping`,
    `cloud`.`host`.`created` AS `created`,
    `cloud`.`host`.`removed` AS `removed`,
    `cloud`.`host`.`resource_state` AS `resource_state`,
    `cloud`.`host`.`mgmt_server_id` AS `mgmt_server_id`,
    `cloud`.`host`.`cpu_sockets` AS `cpu_sockets`,
    `cloud`.`host`.`cpus` AS `cpus`,
    `cloud`.`host`.`speed` AS `speed`,
    `cloud`.`host`.`ram` AS `ram`,
    `cloud`.`cluster`.`id` AS `cluster_id`,
    `cloud`.`cluster`.`uuid` AS `cluster_uuid`,
    `cloud`.`cluster`.`name` AS `cluster_name`,
    `cloud`.`cluster`.`cluster_type` AS `cluster_type`,
    `cloud`.`data_center`.`id` AS `data_center_id`,
    `cloud`.`data_center`.`uuid` AS `data_center_uuid`,
    `cloud`.`data_center`.`name` AS `data_center_name`,
    `cloud`.`data_center`.`networktype` AS `data_center_type`,
    `cloud`.`host_pod_ref`.`id` AS `pod_id`,
    `cloud`.`host_pod_ref`.`uuid` AS `pod_uuid`,
    `cloud`.`host_pod_ref`.`name` AS `pod_name`,
    `cloud`.`host_tags`.`tag` AS `tag`,
    `cloud`.`guest_os_category`.`id` AS `guest_os_category_id`,
    `cloud`.`guest_os_category`.`uuid` AS `guest_os_category_uuid`,
    `cloud`.`guest_os_category`.`name` AS `guest_os_category_name`,
    `mem_caps`.`used_capacity` AS `memory_used_capacity`,
    `mem_caps`.`reserved_capacity` AS `memory_reserved_capacity`,
    `cpu_caps`.`used_capacity` AS `cpu_used_capacity`,
    `cpu_caps`.`reserved_capacity` AS `cpu_reserved_capacity`,
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`
  FROM (((((((((`cloud`.`host`
    LEFT JOIN `cloud`.`cluster` ON ((`cloud`.`host`.`cluster_id` = `cloud`.`cluster`.`id`))) LEFT JOIN
    `cloud`.`data_center` ON ((`cloud`.`host`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN
    `cloud`.`host_pod_ref` ON ((`cloud`.`host`.`pod_id` = `cloud`.`host_pod_ref`.`id`))) LEFT JOIN
    `cloud`.`host_details` ON (((`cloud`.`host`.`id` = `cloud`.`host_details`.`host_id`) AND
                                (`cloud`.`host_details`.`name` = 'guest.os.category.id')))) LEFT JOIN
    `cloud`.`guest_os_category`
      ON ((`cloud`.`guest_os_category`.`id` = cast(`cloud`.`host_details`.`value` AS UNSIGNED)))) LEFT JOIN
    `cloud`.`host_tags` ON ((`cloud`.`host_tags`.`host_id` = `cloud`.`host`.`id`))) LEFT JOIN
    `cloud`.`op_host_capacity` `mem_caps`
      ON (((`cloud`.`host`.`id` = `mem_caps`.`host_id`) AND (`mem_caps`.`capacity_type` = 0)))) LEFT JOIN
    `cloud`.`op_host_capacity` `cpu_caps`
      ON (((`cloud`.`host`.`id` = `cpu_caps`.`host_id`) AND (`cpu_caps`.`capacity_type` = 1)))) LEFT JOIN
    `cloud`.`async_job`
      ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`host`.`id`) AND (`cloud`.`async_job`.`instance_type` = 'Host')
           AND (`cloud`.`async_job`.`job_status` = 0))));

CREATE VIEW `image_store_view` AS
  SELECT
    `cloud`.`image_store`.`id` AS `id`,
    `cloud`.`image_store`.`uuid` AS `uuid`,
    `cloud`.`image_store`.`name` AS `name`,
    `cloud`.`image_store`.`image_provider_name` AS `image_provider_name`,
    `cloud`.`image_store`.`protocol` AS `protocol`,
    `cloud`.`image_store`.`url` AS `url`,
    `cloud`.`image_store`.`scope` AS `scope`,
    `cloud`.`image_store`.`role` AS `role`,
    `cloud`.`image_store`.`removed` AS `removed`,
    `cloud`.`data_center`.`id` AS `data_center_id`,
    `cloud`.`data_center`.`uuid` AS `data_center_uuid`,
    `cloud`.`data_center`.`name` AS `data_center_name`,
    `cloud`.`image_store_details`.`name` AS `detail_name`,
    `cloud`.`image_store_details`.`value` AS `detail_value`
  FROM ((`cloud`.`image_store`
    LEFT JOIN `cloud`.`data_center`
      ON ((`cloud`.`image_store`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN
    `cloud`.`image_store_details` ON ((`cloud`.`image_store_details`.`store_id` = `cloud`.`image_store`.`id`)));

CREATE VIEW `instance_group_view` AS
  SELECT
    `cloud`.`instance_group`.`id` AS `id`,
    `cloud`.`instance_group`.`uuid` AS `uuid`,
    `cloud`.`instance_group`.`name` AS `name`,
    `cloud`.`instance_group`.`removed` AS `removed`,
    `cloud`.`instance_group`.`created` AS `created`,
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
    `cloud`.`projects`.`name` AS `project_name`
  FROM (((`cloud`.`instance_group`
    JOIN `cloud`.`account` ON ((`cloud`.`instance_group`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`account`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`instance_group`.`account_id`)));

CREATE VIEW `project_account_view` AS
  SELECT
    `cloud`.`project_account`.`id` AS `id`,
    `cloud`.`account`.`id` AS `account_id`,
    `cloud`.`account`.`uuid` AS `account_uuid`,
    `cloud`.`account`.`account_name` AS `account_name`,
    `cloud`.`account`.`type` AS `account_type`,
    `cloud`.`project_account`.`account_role` AS `account_role`,
    `cloud`.`projects`.`id` AS `project_id`,
    `cloud`.`projects`.`uuid` AS `project_uuid`,
    `cloud`.`projects`.`name` AS `project_name`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`
  FROM (((`cloud`.`project_account`
    JOIN `cloud`.`account` ON ((`cloud`.`project_account`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`account`.`domain_id` = `cloud`.`domain`.`id`))) JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`id` = `cloud`.`project_account`.`project_id`)));

CREATE VIEW `project_invitation_view` AS
  SELECT
    `cloud`.`project_invitations`.`id` AS `id`,
    `cloud`.`project_invitations`.`uuid` AS `uuid`,
    `cloud`.`project_invitations`.`email` AS `email`,
    `cloud`.`project_invitations`.`created` AS `created`,
    `cloud`.`project_invitations`.`state` AS `state`,
    `cloud`.`projects`.`id` AS `project_id`,
    `cloud`.`projects`.`uuid` AS `project_uuid`,
    `cloud`.`projects`.`name` AS `project_name`,
    `cloud`.`account`.`id` AS `account_id`,
    `cloud`.`account`.`uuid` AS `account_uuid`,
    `cloud`.`account`.`account_name` AS `account_name`,
    `cloud`.`account`.`type` AS `account_type`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`
  FROM (((`cloud`.`project_invitations`
    LEFT JOIN `cloud`.`account` ON ((`cloud`.`project_invitations`.`account_id` = `cloud`.`account`.`id`))) LEFT JOIN
    `cloud`.`domain` ON ((`cloud`.`project_invitations`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN
    `cloud`.`projects` ON ((`cloud`.`projects`.`id` = `cloud`.`project_invitations`.`project_id`)));

CREATE VIEW `project_view` AS
  SELECT
    `cloud`.`projects`.`id` AS `id`,
    `cloud`.`projects`.`uuid` AS `uuid`,
    `cloud`.`projects`.`name` AS `name`,
    `cloud`.`projects`.`display_text` AS `display_text`,
    `cloud`.`projects`.`state` AS `state`,
    `cloud`.`projects`.`removed` AS `removed`,
    `cloud`.`projects`.`created` AS `created`,
    `cloud`.`projects`.`project_account_id` AS `project_account_id`,
    `cloud`.`account`.`account_name` AS `owner`,
    `pacct`.`account_id` AS `account_id`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`,
    `cloud`.`resource_tags`.`id` AS `tag_id`,
    `cloud`.`resource_tags`.`uuid` AS `tag_uuid`,
    `cloud`.`resource_tags`.`key` AS `tag_key`,
    `cloud`.`resource_tags`.`value` AS `tag_value`,
    `cloud`.`resource_tags`.`domain_id` AS `tag_domain_id`,
    `cloud`.`resource_tags`.`account_id` AS `tag_account_id`,
    `cloud`.`resource_tags`.`resource_id` AS `tag_resource_id`,
    `cloud`.`resource_tags`.`resource_uuid` AS `tag_resource_uuid`,
    `cloud`.`resource_tags`.`resource_type` AS `tag_resource_type`,
    `cloud`.`resource_tags`.`customer` AS `tag_customer`
  FROM (((((`cloud`.`projects`
    JOIN `cloud`.`domain` ON ((`cloud`.`projects`.`domain_id` = `cloud`.`domain`.`id`))) JOIN `cloud`.`project_account`
      ON (((`cloud`.`projects`.`id` = `cloud`.`project_account`.`project_id`) AND
           (`cloud`.`project_account`.`account_role` = 'Admin')))) JOIN `cloud`.`account`
      ON ((`cloud`.`account`.`id` = `cloud`.`project_account`.`account_id`))) LEFT JOIN `cloud`.`resource_tags`
      ON (((`cloud`.`resource_tags`.`resource_id` = `cloud`.`projects`.`id`) AND
           (`cloud`.`resource_tags`.`resource_type` = 'Project')))) LEFT JOIN `cloud`.`project_account` `pacct`
      ON ((`cloud`.`projects`.`id` = `pacct`.`project_id`)));

CREATE VIEW `resource_tag_view` AS
  SELECT
    `cloud`.`resource_tags`.`id` AS `id`,
    `cloud`.`resource_tags`.`uuid` AS `uuid`,
    `cloud`.`resource_tags`.`key` AS `key`,
    `cloud`.`resource_tags`.`value` AS `value`,
    `cloud`.`resource_tags`.`resource_id` AS `resource_id`,
    `cloud`.`resource_tags`.`resource_uuid` AS `resource_uuid`,
    `cloud`.`resource_tags`.`resource_type` AS `resource_type`,
    `cloud`.`resource_tags`.`customer` AS `customer`,
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
    `cloud`.`projects`.`name` AS `project_name`
  FROM (((`cloud`.`resource_tags`
    JOIN `cloud`.`account` ON ((`cloud`.`resource_tags`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`resource_tags`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`resource_tags`.`account_id`)));

CREATE VIEW `security_group_view` AS
  SELECT
    `cloud`.`security_group`.`id` AS `id`,
    `cloud`.`security_group`.`name` AS `name`,
    `cloud`.`security_group`.`description` AS `description`,
    `cloud`.`security_group`.`uuid` AS `uuid`,
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
    `cloud`.`security_group_rule`.`id` AS `rule_id`,
    `cloud`.`security_group_rule`.`uuid` AS `rule_uuid`,
    `cloud`.`security_group_rule`.`type` AS `rule_type`,
    `cloud`.`security_group_rule`.`start_port` AS `rule_start_port`,
    `cloud`.`security_group_rule`.`end_port` AS `rule_end_port`,
    `cloud`.`security_group_rule`.`protocol` AS `rule_protocol`,
    `cloud`.`security_group_rule`.`allowed_network_id` AS `rule_allowed_network_id`,
    `cloud`.`security_group_rule`.`allowed_ip_cidr` AS `rule_allowed_ip_cidr`,
    `cloud`.`security_group_rule`.`create_status` AS `rule_create_status`,
    `cloud`.`resource_tags`.`id` AS `tag_id`,
    `cloud`.`resource_tags`.`uuid` AS `tag_uuid`,
    `cloud`.`resource_tags`.`key` AS `tag_key`,
    `cloud`.`resource_tags`.`value` AS `tag_value`,
    `cloud`.`resource_tags`.`domain_id` AS `tag_domain_id`,
    `cloud`.`resource_tags`.`account_id` AS `tag_account_id`,
    `cloud`.`resource_tags`.`resource_id` AS `tag_resource_id`,
    `cloud`.`resource_tags`.`resource_uuid` AS `tag_resource_uuid`,
    `cloud`.`resource_tags`.`resource_type` AS `tag_resource_type`,
    `cloud`.`resource_tags`.`customer` AS `tag_customer`,
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`
  FROM ((((((`cloud`.`security_group`
    LEFT JOIN `cloud`.`security_group_rule`
      ON ((`cloud`.`security_group`.`id` = `cloud`.`security_group_rule`.`security_group_id`))) JOIN `cloud`.`account`
      ON ((`cloud`.`security_group`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`security_group`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`security_group`.`account_id`))) LEFT JOIN
    `cloud`.`resource_tags` ON (((`cloud`.`resource_tags`.`resource_id` = `cloud`.`security_group`.`id`) AND
                                 (`cloud`.`resource_tags`.`resource_type` = 'SecurityGroup')))) LEFT JOIN
    `cloud`.`async_job` ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`security_group`.`id`) AND
                             (`cloud`.`async_job`.`instance_type` = 'SecurityGroup') AND
                             (`cloud`.`async_job`.`job_status` = 0))));

CREATE VIEW `service_offering_view` AS
  SELECT
    `cloud`.`service_offering`.`id` AS `id`,
    `cloud`.`disk_offering`.`uuid` AS `uuid`,
    `cloud`.`disk_offering`.`name` AS `name`,
    `cloud`.`disk_offering`.`display_text` AS `display_text`,
    `cloud`.`disk_offering`.`provisioning_type` AS `provisioning_type`,
    `cloud`.`disk_offering`.`created` AS `created`,
    `cloud`.`disk_offering`.`tags` AS `tags`,
    `cloud`.`disk_offering`.`removed` AS `removed`,
    `cloud`.`disk_offering`.`use_local_storage` AS `use_local_storage`,
    `cloud`.`disk_offering`.`system_use` AS `system_use`,
    `cloud`.`disk_offering`.`customized_iops` AS `customized_iops`,
    `cloud`.`disk_offering`.`min_iops` AS `min_iops`,
    `cloud`.`disk_offering`.`max_iops` AS `max_iops`,
    `cloud`.`disk_offering`.`hv_ss_reserve` AS `hv_ss_reserve`,
    `cloud`.`disk_offering`.`bytes_read_rate` AS `bytes_read_rate`,
    `cloud`.`disk_offering`.`bytes_write_rate` AS `bytes_write_rate`,
    `cloud`.`disk_offering`.`iops_read_rate` AS `iops_read_rate`,
    `cloud`.`disk_offering`.`iops_write_rate` AS `iops_write_rate`,
    `cloud`.`disk_offering`.`cache_mode` AS `cache_mode`,
    `cloud`.`service_offering`.`cpu` AS `cpu`,
    `cloud`.`service_offering`.`speed` AS `speed`,
    `cloud`.`service_offering`.`ram_size` AS `ram_size`,
    `cloud`.`service_offering`.`nw_rate` AS `nw_rate`,
    `cloud`.`service_offering`.`mc_rate` AS `mc_rate`,
    `cloud`.`service_offering`.`ha_enabled` AS `ha_enabled`,
    `cloud`.`service_offering`.`limit_cpu_use` AS `limit_cpu_use`,
    `cloud`.`service_offering`.`host_tag` AS `host_tag`,
    `cloud`.`service_offering`.`default_use` AS `default_use`,
    `cloud`.`service_offering`.`vm_type` AS `vm_type`,
    `cloud`.`service_offering`.`sort_key` AS `sort_key`,
    `cloud`.`service_offering`.`is_volatile` AS `is_volatile`,
    `cloud`.`service_offering`.`deployment_planner` AS `deployment_planner`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`
  FROM ((`cloud`.`service_offering`
    JOIN `cloud`.`disk_offering` ON ((`cloud`.`service_offering`.`id` = `cloud`.`disk_offering`.`id`))) LEFT JOIN
    `cloud`.`domain` ON ((`cloud`.`disk_offering`.`domain_id` = `cloud`.`domain`.`id`)))
  WHERE (`cloud`.`disk_offering`.`state` = 'Active');

CREATE VIEW `storage_pool_view` AS
  SELECT
    `cloud`.`storage_pool`.`id` AS `id`,
    `cloud`.`storage_pool`.`uuid` AS `uuid`,
    `cloud`.`storage_pool`.`name` AS `name`,
    `cloud`.`storage_pool`.`status` AS `status`,
    `cloud`.`storage_pool`.`path` AS `path`,
    `cloud`.`storage_pool`.`pool_type` AS `pool_type`,
    `cloud`.`storage_pool`.`host_address` AS `host_address`,
    `cloud`.`storage_pool`.`created` AS `created`,
    `cloud`.`storage_pool`.`removed` AS `removed`,
    `cloud`.`storage_pool`.`capacity_bytes` AS `capacity_bytes`,
    `cloud`.`storage_pool`.`capacity_iops` AS `capacity_iops`,
    `cloud`.`storage_pool`.`scope` AS `scope`,
    `cloud`.`storage_pool`.`hypervisor` AS `hypervisor`,
    `cloud`.`storage_pool`.`storage_provider_name` AS `storage_provider_name`,
    `cloud`.`cluster`.`id` AS `cluster_id`,
    `cloud`.`cluster`.`uuid` AS `cluster_uuid`,
    `cloud`.`cluster`.`name` AS `cluster_name`,
    `cloud`.`cluster`.`cluster_type` AS `cluster_type`,
    `cloud`.`data_center`.`id` AS `data_center_id`,
    `cloud`.`data_center`.`uuid` AS `data_center_uuid`,
    `cloud`.`data_center`.`name` AS `data_center_name`,
    `cloud`.`data_center`.`networktype` AS `data_center_type`,
    `cloud`.`host_pod_ref`.`id` AS `pod_id`,
    `cloud`.`host_pod_ref`.`uuid` AS `pod_uuid`,
    `cloud`.`host_pod_ref`.`name` AS `pod_name`,
    `cloud`.`storage_pool_details`.`name` AS `tag`,
    `cloud`.`op_host_capacity`.`used_capacity` AS `disk_used_capacity`,
    `cloud`.`op_host_capacity`.`reserved_capacity` AS `disk_reserved_capacity`,
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`
  FROM ((((((`cloud`.`storage_pool`
    LEFT JOIN `cloud`.`cluster` ON ((`cloud`.`storage_pool`.`cluster_id` = `cloud`.`cluster`.`id`))) LEFT JOIN
    `cloud`.`data_center` ON ((`cloud`.`storage_pool`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN
    `cloud`.`host_pod_ref` ON ((`cloud`.`storage_pool`.`pod_id` = `cloud`.`host_pod_ref`.`id`))) LEFT JOIN
    `cloud`.`storage_pool_details` ON (((`cloud`.`storage_pool_details`.`pool_id` = `cloud`.`storage_pool`.`id`) AND
                                        (`cloud`.`storage_pool_details`.`value` = 'true')))) LEFT JOIN
    `cloud`.`op_host_capacity` ON (((`cloud`.`storage_pool`.`id` = `cloud`.`op_host_capacity`.`host_id`) AND
                                    (`cloud`.`op_host_capacity`.`capacity_type` IN (3, 9))))) LEFT JOIN
    `cloud`.`async_job` ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`storage_pool`.`id`) AND
                             (`cloud`.`async_job`.`instance_type` = 'StoragePool') AND
                             (`cloud`.`async_job`.`job_status` = 0))));

CREATE VIEW `storage_tag_view` AS
  SELECT
    `cloud`.`storage_pool_details`.`id` AS `id`,
    `cloud`.`storage_pool_details`.`pool_id` AS `pool_id`,
    `cloud`.`storage_pool_details`.`name` AS `name`
  FROM `cloud`.`storage_pool_details`
  WHERE (`cloud`.`storage_pool_details`.`value` = 'true');

CREATE VIEW `template_view` AS
  SELECT
    `cloud`.`vm_template`.`id` AS `id`,
    `cloud`.`vm_template`.`uuid` AS `uuid`,
    `cloud`.`vm_template`.`unique_name` AS `unique_name`,
    `cloud`.`vm_template`.`name` AS `name`,
    `cloud`.`vm_template`.`public` AS `public`,
    `cloud`.`vm_template`.`featured` AS `featured`,
    `cloud`.`vm_template`.`type` AS `type`,
    `cloud`.`vm_template`.`hvm` AS `hvm`,
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

CREATE VIEW `user_view` AS
  SELECT
    `cloud`.`user`.`id` AS `id`,
    `cloud`.`user`.`uuid` AS `uuid`,
    `cloud`.`user`.`username` AS `username`,
    `cloud`.`user`.`password` AS `password`,
    `cloud`.`user`.`firstname` AS `firstname`,
    `cloud`.`user`.`lastname` AS `lastname`,
    `cloud`.`user`.`email` AS `email`,
    `cloud`.`user`.`state` AS `state`,
    `cloud`.`user`.`api_key` AS `api_key`,
    `cloud`.`user`.`secret_key` AS `secret_key`,
    `cloud`.`user`.`created` AS `created`,
    `cloud`.`user`.`removed` AS `removed`,
    `cloud`.`user`.`timezone` AS `timezone`,
    `cloud`.`user`.`registration_token` AS `registration_token`,
    `cloud`.`user`.`is_registered` AS `is_registered`,
    `cloud`.`user`.`incorrect_login_attempts` AS `incorrect_login_attempts`,
    `cloud`.`user`.`default` AS `default`,
    `cloud`.`account`.`id` AS `account_id`,
    `cloud`.`account`.`uuid` AS `account_uuid`,
    `cloud`.`account`.`account_name` AS `account_name`,
    `cloud`.`account`.`type` AS `account_type`,
    `cloud`.`domain`.`id` AS `domain_id`,
    `cloud`.`domain`.`uuid` AS `domain_uuid`,
    `cloud`.`domain`.`name` AS `domain_name`,
    `cloud`.`domain`.`path` AS `domain_path`,
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`
  FROM (((`cloud`.`user`
    JOIN `cloud`.`account` ON ((`cloud`.`user`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`account`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`async_job`
      ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`user`.`id`) AND (`cloud`.`async_job`.`instance_type` = 'User')
           AND (`cloud`.`async_job`.`job_status` = 0))));

CREATE VIEW `user_vm_view` AS
  SELECT
    `cloud`.`vm_instance`.`id` AS `id`,
    `cloud`.`vm_instance`.`name` AS `name`,
    `cloud`.`user_vm`.`display_name` AS `display_name`,
    `cloud`.`user_vm`.`user_data` AS `user_data`,
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
    `cloud`.`instance_group`.`id` AS `instance_group_id`,
    `cloud`.`instance_group`.`uuid` AS `instance_group_uuid`,
    `cloud`.`instance_group`.`name` AS `instance_group_name`,
    `cloud`.`vm_instance`.`uuid` AS `uuid`,
    `cloud`.`vm_instance`.`user_id` AS `user_id`,
    `cloud`.`vm_instance`.`last_host_id` AS `last_host_id`,
    `cloud`.`vm_instance`.`vm_type` AS `type`,
    `cloud`.`vm_instance`.`limit_cpu_use` AS `limit_cpu_use`,
    `cloud`.`vm_instance`.`created` AS `created`,
    `cloud`.`vm_instance`.`state` AS `state`,
    `cloud`.`vm_instance`.`removed` AS `removed`,
    `cloud`.`vm_instance`.`ha_enabled` AS `ha_enabled`,
    `cloud`.`vm_instance`.`hypervisor_type` AS `hypervisor_type`,
    `cloud`.`vm_instance`.`instance_name` AS `instance_name`,
    `cloud`.`vm_instance`.`guest_os_id` AS `guest_os_id`,
    `cloud`.`vm_instance`.`display_vm` AS `display_vm`,
    `cloud`.`guest_os`.`uuid` AS `guest_os_uuid`,
    `cloud`.`vm_instance`.`pod_id` AS `pod_id`,
    `cloud`.`host_pod_ref`.`uuid` AS `pod_uuid`,
    `cloud`.`vm_instance`.`private_ip_address` AS `private_ip_address`,
    `cloud`.`vm_instance`.`private_mac_address` AS `private_mac_address`,
    `cloud`.`vm_instance`.`vm_type` AS `vm_type`,
    `cloud`.`data_center`.`id` AS `data_center_id`,
    `cloud`.`data_center`.`uuid` AS `data_center_uuid`,
    `cloud`.`data_center`.`name` AS `data_center_name`,
    `cloud`.`data_center`.`is_security_group_enabled` AS `security_group_enabled`,
    `cloud`.`data_center`.`networktype` AS `data_center_type`,
    `cloud`.`host`.`id` AS `host_id`,
    `cloud`.`host`.`uuid` AS `host_uuid`,
    `cloud`.`host`.`name` AS `host_name`,
    `cloud`.`vm_template`.`id` AS `template_id`,
    `cloud`.`vm_template`.`uuid` AS `template_uuid`,
    `cloud`.`vm_template`.`name` AS `template_name`,
    `cloud`.`vm_template`.`display_text` AS `template_display_text`,
    `cloud`.`vm_template`.`enable_password` AS `password_enabled`,
    `iso`.`id` AS `iso_id`,
    `iso`.`uuid` AS `iso_uuid`,
    `iso`.`name` AS `iso_name`,
    `iso`.`display_text` AS `iso_display_text`,
    `cloud`.`service_offering`.`id` AS `service_offering_id`,
    `svc_disk_offering`.`uuid` AS `service_offering_uuid`,
    `cloud`.`disk_offering`.`uuid` AS `disk_offering_uuid`,
    `cloud`.`disk_offering`.`id` AS `disk_offering_id`,
    (CASE WHEN isnull(`cloud`.`service_offering`.`cpu`)
      THEN `custom_cpu`.`value`
     ELSE `cloud`.`service_offering`.`cpu` END) AS `cpu`,
    (CASE WHEN isnull(`cloud`.`service_offering`.`speed`)
      THEN `custom_speed`.`value`
     ELSE `cloud`.`service_offering`.`speed` END) AS `speed`,
    (CASE WHEN isnull(`cloud`.`service_offering`.`ram_size`)
      THEN `custom_ram_size`.`value`
     ELSE `cloud`.`service_offering`.`ram_size` END) AS `ram_size`,
    `svc_disk_offering`.`name` AS `service_offering_name`,
    `cloud`.`disk_offering`.`name` AS `disk_offering_name`,
    `cloud`.`storage_pool`.`id` AS `pool_id`,
    `cloud`.`storage_pool`.`uuid` AS `pool_uuid`,
    `cloud`.`storage_pool`.`pool_type` AS `pool_type`,
    `cloud`.`volumes`.`id` AS `volume_id`,
    `cloud`.`volumes`.`uuid` AS `volume_uuid`,
    `cloud`.`volumes`.`device_id` AS `volume_device_id`,
    `cloud`.`volumes`.`volume_type` AS `volume_type`,
    `cloud`.`security_group`.`id` AS `security_group_id`,
    `cloud`.`security_group`.`uuid` AS `security_group_uuid`,
    `cloud`.`security_group`.`name` AS `security_group_name`,
    `cloud`.`security_group`.`description` AS `security_group_description`,
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
    `cloud`.`networks`.`uuid` AS `network_uuid`,
    `cloud`.`networks`.`name` AS `network_name`,
    `cloud`.`networks`.`traffic_type` AS `traffic_type`,
    `cloud`.`networks`.`guest_type` AS `guest_type`,
    `cloud`.`user_ip_address`.`id` AS `public_ip_id`,
    `cloud`.`user_ip_address`.`uuid` AS `public_ip_uuid`,
    `cloud`.`user_ip_address`.`public_ip_address` AS `public_ip_address`,
    `cloud`.`ssh_keypairs`.`keypair_name` AS `keypair_name`,
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
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`,
    `cloud`.`affinity_group`.`id` AS `affinity_group_id`,
    `cloud`.`affinity_group`.`uuid` AS `affinity_group_uuid`,
    `cloud`.`affinity_group`.`name` AS `affinity_group_name`,
    `cloud`.`affinity_group`.`description` AS `affinity_group_description`,
    `cloud`.`vm_instance`.`dynamically_scalable` AS `dynamically_scalable`
  FROM ((((((((((((((((((((((((((((((((`cloud`.`user_vm`
    JOIN `cloud`.`vm_instance`
      ON (((`cloud`.`vm_instance`.`id` = `cloud`.`user_vm`.`id`) AND isnull(`cloud`.`vm_instance`.`removed`)))) JOIN
    `cloud`.`account` ON ((`cloud`.`vm_instance`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`vm_instance`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`guest_os`
      ON ((`cloud`.`vm_instance`.`guest_os_id` = `cloud`.`guest_os`.`id`))) LEFT JOIN `cloud`.`host_pod_ref`
      ON ((`cloud`.`vm_instance`.`pod_id` = `cloud`.`host_pod_ref`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`account`.`id`))) LEFT JOIN `cloud`.`instance_group_vm_map`
      ON ((`cloud`.`vm_instance`.`id` = `cloud`.`instance_group_vm_map`.`instance_id`))) LEFT JOIN
    `cloud`.`instance_group`
      ON ((`cloud`.`instance_group_vm_map`.`group_id` = `cloud`.`instance_group`.`id`))) LEFT JOIN `cloud`.`data_center`
      ON ((`cloud`.`vm_instance`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN `cloud`.`host`
      ON ((`cloud`.`vm_instance`.`host_id` = `cloud`.`host`.`id`))) LEFT JOIN `cloud`.`vm_template`
      ON ((`cloud`.`vm_instance`.`vm_template_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN `cloud`.`vm_template` `iso`
      ON ((`iso`.`id` = `cloud`.`user_vm`.`iso_id`))) LEFT JOIN `cloud`.`service_offering`
      ON ((`cloud`.`vm_instance`.`service_offering_id` = `cloud`.`service_offering`.`id`))) LEFT JOIN
    `cloud`.`disk_offering` `svc_disk_offering`
      ON ((`cloud`.`vm_instance`.`service_offering_id` = `svc_disk_offering`.`id`))) LEFT JOIN `cloud`.`disk_offering`
      ON ((`cloud`.`vm_instance`.`disk_offering_id` = `cloud`.`disk_offering`.`id`))) LEFT JOIN `cloud`.`volumes`
      ON ((`cloud`.`vm_instance`.`id` = `cloud`.`volumes`.`instance_id`))) LEFT JOIN `cloud`.`storage_pool`
      ON ((`cloud`.`volumes`.`pool_id` = `cloud`.`storage_pool`.`id`))) LEFT JOIN `cloud`.`security_group_vm_map`
      ON ((`cloud`.`vm_instance`.`id` = `cloud`.`security_group_vm_map`.`instance_id`))) LEFT JOIN
    `cloud`.`security_group`
      ON ((`cloud`.`security_group_vm_map`.`security_group_id` = `cloud`.`security_group`.`id`))) LEFT JOIN
    `cloud`.`nics`
      ON (((`cloud`.`vm_instance`.`id` = `cloud`.`nics`.`instance_id`) AND isnull(`cloud`.`nics`.`removed`)))) LEFT JOIN
    `cloud`.`networks` ON ((`cloud`.`nics`.`network_id` = `cloud`.`networks`.`id`))) LEFT JOIN `cloud`.`vpc`
      ON (((`cloud`.`networks`.`vpc_id` = `cloud`.`vpc`.`id`) AND isnull(`cloud`.`vpc`.`removed`)))) LEFT JOIN
    `cloud`.`user_ip_address` ON ((`cloud`.`user_ip_address`.`vm_id` = `cloud`.`vm_instance`.`id`))) LEFT JOIN
    `cloud`.`user_vm_details` `ssh_details` ON (((`ssh_details`.`vm_id` = `cloud`.`vm_instance`.`id`) AND
                                                 (`ssh_details`.`name` = 'SSH.PublicKey')))) LEFT JOIN
    `cloud`.`ssh_keypairs` ON (((`cloud`.`ssh_keypairs`.`public_key` = `ssh_details`.`value`) AND
                                (`cloud`.`ssh_keypairs`.`account_id` = `cloud`.`account`.`id`)))) LEFT JOIN
    `cloud`.`resource_tags` ON (((`cloud`.`resource_tags`.`resource_id` = `cloud`.`vm_instance`.`id`) AND
                                 (`cloud`.`resource_tags`.`resource_type` = 'UserVm')))) LEFT JOIN `cloud`.`async_job`
      ON (((`cloud`.`async_job`.`instance_id` = `cloud`.`vm_instance`.`id`) AND
           (`cloud`.`async_job`.`instance_type` = 'VirtualMachine') AND
           (`cloud`.`async_job`.`job_status` = 0)))) LEFT JOIN `cloud`.`affinity_group_vm_map`
      ON ((`cloud`.`vm_instance`.`id` = `cloud`.`affinity_group_vm_map`.`instance_id`))) LEFT JOIN
    `cloud`.`affinity_group`
      ON ((`cloud`.`affinity_group_vm_map`.`affinity_group_id` = `cloud`.`affinity_group`.`id`))) LEFT JOIN
    `cloud`.`user_vm_details` `custom_cpu`
      ON (((`custom_cpu`.`vm_id` = `cloud`.`vm_instance`.`id`) AND (`custom_cpu`.`name` = 'CpuNumber')))) LEFT JOIN
    `cloud`.`user_vm_details` `custom_speed`
      ON (((`custom_speed`.`vm_id` = `cloud`.`vm_instance`.`id`) AND (`custom_speed`.`name` = 'CpuSpeed')))) LEFT JOIN
    `cloud`.`user_vm_details` `custom_ram_size`
      ON (((`custom_ram_size`.`vm_id` = `cloud`.`vm_instance`.`id`) AND (`custom_ram_size`.`name` = 'memory'))));

CREATE VIEW `volume_view` AS
  SELECT
    `cloud`.`volumes`.`id` AS `id`,
    `cloud`.`volumes`.`uuid` AS `uuid`,
    `cloud`.`volumes`.`name` AS `name`,
    `cloud`.`volumes`.`device_id` AS `device_id`,
    `cloud`.`volumes`.`volume_type` AS `volume_type`,
    `cloud`.`volumes`.`provisioning_type` AS `provisioning_type`,
    `cloud`.`volumes`.`size` AS `size`,
    `cloud`.`volumes`.`min_iops` AS `min_iops`,
    `cloud`.`volumes`.`max_iops` AS `max_iops`,
    `cloud`.`volumes`.`created` AS `created`,
    `cloud`.`volumes`.`state` AS `state`,
    `cloud`.`volumes`.`attached` AS `attached`,
    `cloud`.`volumes`.`removed` AS `removed`,
    `cloud`.`volumes`.`pod_id` AS `pod_id`,
    `cloud`.`volumes`.`display_volume` AS `display_volume`,
    `cloud`.`volumes`.`format` AS `format`,
    `cloud`.`volumes`.`path` AS `path`,
    `cloud`.`volumes`.`chain_info` AS `chain_info`,
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
    `cloud`.`data_center`.`networktype` AS `data_center_type`,
    `cloud`.`vm_instance`.`id` AS `vm_id`,
    `cloud`.`vm_instance`.`uuid` AS `vm_uuid`,
    `cloud`.`vm_instance`.`name` AS `vm_name`,
    `cloud`.`vm_instance`.`state` AS `vm_state`,
    `cloud`.`vm_instance`.`vm_type` AS `vm_type`,
    `cloud`.`user_vm`.`display_name` AS `vm_display_name`,
    `cloud`.`volume_store_ref`.`size` AS `volume_store_size`,
    `cloud`.`volume_store_ref`.`download_pct` AS `download_pct`,
    `cloud`.`volume_store_ref`.`download_state` AS `download_state`,
    `cloud`.`volume_store_ref`.`error_str` AS `error_str`,
    `cloud`.`volume_store_ref`.`created` AS `created_on_store`,
    `cloud`.`disk_offering`.`id` AS `disk_offering_id`,
    `cloud`.`disk_offering`.`uuid` AS `disk_offering_uuid`,
    `cloud`.`disk_offering`.`name` AS `disk_offering_name`,
    `cloud`.`disk_offering`.`display_text` AS `disk_offering_display_text`,
    `cloud`.`disk_offering`.`use_local_storage` AS `use_local_storage`,
    `cloud`.`disk_offering`.`system_use` AS `system_use`,
    `cloud`.`disk_offering`.`bytes_read_rate` AS `bytes_read_rate`,
    `cloud`.`disk_offering`.`bytes_write_rate` AS `bytes_write_rate`,
    `cloud`.`disk_offering`.`iops_read_rate` AS `iops_read_rate`,
    `cloud`.`disk_offering`.`iops_write_rate` AS `iops_write_rate`,
    `cloud`.`disk_offering`.`cache_mode` AS `cache_mode`,
    `cloud`.`storage_pool`.`id` AS `pool_id`,
    `cloud`.`storage_pool`.`uuid` AS `pool_uuid`,
    `cloud`.`storage_pool`.`name` AS `pool_name`,
    `cloud`.`cluster`.`hypervisor_type` AS `hypervisor_type`,
    `cloud`.`vm_template`.`id` AS `template_id`,
    `cloud`.`vm_template`.`uuid` AS `template_uuid`,
    `cloud`.`vm_template`.`extractable` AS `extractable`,
    `cloud`.`vm_template`.`type` AS `template_type`,
    `cloud`.`vm_template`.`name` AS `template_name`,
    `cloud`.`vm_template`.`display_text` AS `template_display_text`,
    `iso`.`id` AS `iso_id`,
    `iso`.`uuid` AS `iso_uuid`,
    `iso`.`name` AS `iso_name`,
    `iso`.`display_text` AS `iso_display_text`,
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
    `cloud`.`async_job`.`id` AS `job_id`,
    `cloud`.`async_job`.`uuid` AS `job_uuid`,
    `cloud`.`async_job`.`job_status` AS `job_status`,
    `cloud`.`async_job`.`account_id` AS `job_account_id`
  FROM ((((((((((((((`cloud`.`volumes`
    JOIN `cloud`.`account` ON ((`cloud`.`volumes`.`account_id` = `cloud`.`account`.`id`))) JOIN `cloud`.`domain`
      ON ((`cloud`.`volumes`.`domain_id` = `cloud`.`domain`.`id`))) LEFT JOIN `cloud`.`projects`
      ON ((`cloud`.`projects`.`project_account_id` = `cloud`.`account`.`id`))) LEFT JOIN `cloud`.`data_center`
      ON ((`cloud`.`volumes`.`data_center_id` = `cloud`.`data_center`.`id`))) LEFT JOIN `cloud`.`vm_instance`
      ON ((`cloud`.`volumes`.`instance_id` = `cloud`.`vm_instance`.`id`))) LEFT JOIN `cloud`.`user_vm`
      ON ((`cloud`.`user_vm`.`id` = `cloud`.`vm_instance`.`id`))) LEFT JOIN `cloud`.`volume_store_ref`
      ON ((`cloud`.`volumes`.`id` = `cloud`.`volume_store_ref`.`volume_id`))) LEFT JOIN `cloud`.`disk_offering`
      ON ((`cloud`.`volumes`.`disk_offering_id` = `cloud`.`disk_offering`.`id`))) LEFT JOIN `cloud`.`storage_pool`
      ON ((`cloud`.`volumes`.`pool_id` = `cloud`.`storage_pool`.`id`))) LEFT JOIN `cloud`.`cluster`
      ON ((`cloud`.`storage_pool`.`cluster_id` = `cloud`.`cluster`.`id`))) LEFT JOIN `cloud`.`vm_template`
      ON ((`cloud`.`volumes`.`template_id` = `cloud`.`vm_template`.`id`))) LEFT JOIN `cloud`.`vm_template` `iso`
      ON ((`iso`.`id` = `cloud`.`volumes`.`iso_id`))) LEFT JOIN `cloud`.`resource_tags`
      ON (((`cloud`.`resource_tags`.`resource_id` = `cloud`.`volumes`.`id`) AND
           (`cloud`.`resource_tags`.`resource_type` = 'Volume')))) LEFT JOIN `cloud`.`async_job` ON ((
    (`cloud`.`async_job`.`instance_id` = `cloud`.`volumes`.`id`) AND (`cloud`.`async_job`.`instance_type` = 'Volume')
    AND (`cloud`.`async_job`.`job_status` = 0))));
