-- Cleanup unused tables
DROP TABLE IF EXISTS vmware_data_center_zone_map;
DROP TABLE IF EXISTS vmware_data_center;

DROP TABLE IF EXISTS ucs_blade;
DROP TABLE IF EXISTS ucs_manager;

DROP TABLE IF EXISTS virtual_supervisor_module;

DROP TABLE IF EXISTS user_vm_clone_setting;

DROP TABLE IF EXISTS stack_maid;

DROP TABLE IF EXISTS saml_token;

DROP TABLE IF EXISTS ovs_providers;
DROP TABLE IF EXISTS ovs_tunnel_interface;
DROP TABLE IF EXISTS ovs_tunnel_network;

DROP TABLE IF EXISTS op_vpc_distributed_router_sequence_no;

DROP TABLE IF EXISTS op_host_upgrade;

DROP TABLE IF EXISTS object_datastore_ref;

DROP TABLE IF EXISTS legacy_zones;

DROP TABLE IF EXISTS external_nuage_vsp_devices;
DROP TABLE IF EXISTS external_stratosphere_ssp_credentials;
DROP TABLE IF EXISTS external_stratosphere_ssp_tenants;
DROP TABLE IF EXISTS external_stratosphere_ssp_uuids;
