--;
-- Schema cleanup from 5.1.0 to 5.1.1;
--;

-- Remove unused table async_job_journal
DROP TABLE IF EXISTS `cloud`.`async_job_journal`;

-- Remove unused table cluster_vsm_map
DROP TABLE IF EXISTS `cloud`.`cluster_vsm_map`;

-- Remove unused table cmd_exec_log
DROP TABLE IF EXISTS `cloud`.`cmd_exec_log`;

-- Remove unused table elastic_lb_vm_map
DROP TABLE IF EXISTS `cloud`.`elastic_lb_vm_map`;
