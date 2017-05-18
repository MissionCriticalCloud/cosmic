--
-- Schema upgrade from 5.3.4 to 5.3.5;
--

-- Manufacturer field
ALTER table guest_os ADD `manufacturer_string` varchar(64) DEFAULT "Mission Critical Cloud" COMMENT 'String to put in the Manufacturer field in the XML of a KVM VM';

