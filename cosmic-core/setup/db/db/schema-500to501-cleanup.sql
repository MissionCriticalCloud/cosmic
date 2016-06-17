--;
-- Schema cleanup from 5.0.0 to 5.0.1;
--;

# Remove LXC templates
DELETE FROM `cloud`.`vm_template` WHERE hypervisor_type = 'LXC';
DELETE FROM `cloud`.`hypervisor_capabilities` WHERE hypervisor_type = 'LXC';

# Remove LXC global settings
DELETE FROM `cloud`.`configuration` WHERE name = 'router.template.lxc';

# Remove LXC related column on physical_network_traffic_types table
ALTER TABLE `cloud`.`physical_network_traffic_types` DROP COLUMN `lxc_network_label`;

# Remove LXC
DELETE FROM `cloud`.`guest_os_hypervisor` where hypervisor_type = 'LXC';

# Remove HyperV and VMware templates
DELETE FROM `cloud`.`vm_template` WHERE hypervisor_type = 'Hyperv';
DELETE FROM `cloud`.`vm_template` WHERE hypervisor_type = 'VMware';
DELETE FROM `cloud`.`hypervisor_capabilities` WHERE hypervisor_type = 'Hyperv';
DELETE FROM `cloud`.`hypervisor_capabilities` WHERE hypervisor_type = 'VMware';

# Remove HyperV and VMware global settings
DELETE FROM `cloud`.`configuration` WHERE name = 'hyperv.guest.network.device';
DELETE FROM `cloud`.`configuration` WHERE name = 'hyperv.private.network.device';
DELETE FROM `cloud`.`configuration` WHERE name = 'hyperv.public.network.device';
DELETE FROM `cloud`.`configuration` WHERE name = 'router.template.hyperv';
DELETE FROM `cloud`.`configuration` WHERE name = 'router.template.vmware';

# Remove HyperV related column on physical_network_traffic_types table
ALTER TABLE `cloud`.`physical_network_traffic_types` DROP COLUMN `hyperv_network_label`;

# Remove Quota column
ALTER TABLE `cloud_usage`.`cloud_usage` DROP COLUMN `quota_calculated`;

# Remove quota global settings
DELETE FROM `cloud`.`configuration` WHERE value = 'quota.enable.service';

# Remove port_profile table - it was used by VMware
DROP TABLE IF EXISTS `cloud`.`port_profile`;

# Remove VMware related column on physical_network_traffic_types table
ALTER TABLE `cloud`.`physical_network_traffic_types` DROP COLUMN `vmware_network_label`; 

# Remove netscaler
DROP TABLE IF EXISTS `cloud`.`netscaler_pod_ref`;