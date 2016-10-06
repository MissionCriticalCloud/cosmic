--;
-- Schema cleanup from 5.1.0 to 5.1.1;
--;

-- Remove unused table async_job_journal
DROP TABLE IF EXISTS async_job_journal;

-- Remove unused table cluster_vsm_map
DROP TABLE IF EXISTS cluster_vsm_map;