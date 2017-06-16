--
-- Schema upgrade from 5.3.6 to 5.3.7;
--

UPDATE `cloud`.`hypervisor_capabilities` SET `vm_snapshot_enabled` = 1 WHERE `hypervisor_type` = 'KVM' AND `hypervisor_version` = 'default';
