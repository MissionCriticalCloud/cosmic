-- Add setting
INSERT IGNORE INTO `configuration` (`category`, `instance`, `component`, `name`, `value`, `description`, `default_value`, `updated`, `scope`, `is_dynamic`)
VALUES
	('Advanced', 'DEFAULT', 'management-server', 'systemvm.setrfc1918routes', 'true', 'Set RFC1918 routes to MGT nic on SystemVM', 'true', NULL, NULL, 1);
