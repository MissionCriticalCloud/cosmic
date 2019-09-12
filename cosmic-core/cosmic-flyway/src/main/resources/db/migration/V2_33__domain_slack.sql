ALTER TABLE `cloud`.`domain` ADD `slack_channel_name` varchar(255);

-- Update the domain_view
DROP VIEW IF EXISTS `domain_view`;

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
    `cloud`.`domain`.`slack_channel_name` AS `slack_channel_name`,
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
