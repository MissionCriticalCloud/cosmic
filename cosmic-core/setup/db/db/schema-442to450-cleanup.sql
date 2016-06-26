--
-- Schema cleanup from 4.4.2 to 4.5.0
--

UPDATE `cloud`.`configuration`
SET name = 'router.template.xenserver'
Where name = 'router.template.xen';

UPDATE `cloud`.`configuration`
SET name = 'xenserver.nics.max',
description = 'Maximum allowed nics for Vms created on XenServer'
Where name = 'xen.nics.max';

UPDATE `cloud`.`configuration`
SET value = 'XenServer'
Where value = 'Xen';
