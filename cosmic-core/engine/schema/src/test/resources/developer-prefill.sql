-- Add a default ROOT domain
use cloud;

INSERT INTO `cloud`.`domain` (id, uuid, name, parent, path, owner) VALUES
            (1, UUID(), 'ROOT', NULL, '/', 2);

-- Add system and admin accounts
INSERT INTO `cloud`.`account` (id, uuid, account_name, type, domain_id, state) VALUES
            (1, UUID(), 'system', 1, 1, 'enabled');

INSERT INTO `cloud`.`account` (id, uuid, account_name, type, domain_id, state) VALUES
            (2, UUID(), 'admin', 1, 1, 'enabled');

-- Add system user
INSERT INTO `cloud`.`user` (id, uuid, username, password, account_id, firstname,
            lastname, email, state, created) VALUES (1, UUID(), 'system', RAND(),
            '1', 'system', 'cloud', NULL, 'enabled', NOW());

-- Add system user with encrypted password=password
INSERT INTO `cloud`.`user` (id, uuid, username, password, account_id, firstname,
            lastname, email, state, created) VALUES (2, UUID(), 'admin', '5f4dcc3b5aa765d61d8327deb882cf99',
            '2', 'Admin', 'User', 'admin@mailprovider.com', 'disabled', NOW());

-- Add configurations
INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Hidden', 'DEFAULT', 'management-server', 'init', 'false');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'integration.api.port', '8096');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'secstorage.allowed.internal.sites', '0.0.0.0/0');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'account.cleanup.interval', '60');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'expunge.delay', '60');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'expunge.interval', '60');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'cluster.cpu.allocated.capacity.disablethreshold', '0.95');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'cluster.memory.allocated.capacity.disablethreshold', '0.95');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'pool.storage.allocated.capacity.disablethreshold', '0.95');

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'pool.storage.capacity.disablethreshold', '0.95');

-- Add developer configuration entry; allows management server to be run as a user other than "cloud"
INSERT INTO `cloud`.`configuration` (category, instance, component, name, value)
            VALUES ('Advanced', 'DEFAULT', 'management-server',
            'developer', 'true');

commit;
