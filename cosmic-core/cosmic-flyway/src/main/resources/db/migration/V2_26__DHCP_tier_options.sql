ALTER TABLE networks
    ADD COLUMN `dhcp_tftp_server` varchar(255)
    DEFAULT NULL COMMENT 'DHCP option 66: tftp-server';

ALTER TABLE networks
    ADD COLUMN `dhcp_bootfile_name` varchar(255)
    DEFAULT NULL COMMENT 'DHCP option 67: bootfile-name';
