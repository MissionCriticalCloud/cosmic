--;
-- Schema upgrade from 4.8.0 to 5.0.0;
--;

ALTER TABLE `event` ADD INDEX `archived` (`archived`) using HASH;
ALTER TABLE `event` ADD INDEX `state` (`state`) using HASH;

# Update Hypervisor list in order to remove HyperV and VMware
UPDATE configuration SET value='KVM,XenServer,LXC' WHERE name='hypervisor.list';