--
-- Schema cleanup from 4.5.2 to 4.6.0
--

DELETE FROM `cloud`.`configuration` where name='router.reboot.when.outofband.migrated';

