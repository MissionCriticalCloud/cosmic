--;
-- Schema cleanup from 4.8.0 to 5.0.0;
--;

# Remove NetApp plugin
DROP TABLE IF EXISTS `cloud`.`netapp_lun`;
DROP TABLE IF EXISTS `cloud`.`netapp_volume`;
DROP TABLE IF EXISTS `cloud`.`netapp_pool`;

# Remove BigSwitch plugin
DROP TABLE IF EXISTS `cloud`.`external_bigswitch_vns_devices`;
DROP TABLE IF EXISTS `cloud`.`external_bigswitch_bcf_devices`;

# Remove Brocade plugin
DROP TABLE IF EXISTS `cloud`.`brocade_network_vlan_map`;
DROP TABLE IF EXISTS `cloud`.`external_brocade_vcs_devices`;