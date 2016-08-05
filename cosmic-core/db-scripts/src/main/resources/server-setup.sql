/* This file specifies default values that go into the database, before the Management Server is run. */

/* Root Domain */
INSERT INTO `cloud`.`domain` (id, uuid, name, parent, path, owner) VALUES (1, UUID(), 'ROOT', NULL, '/', 2);

/* Configuration Table */

INSERT INTO `cloud`.`configuration` (category, instance, component, name, value, description) VALUES ('Hidden', 'DEFAULT', 'none', 'init', null, null);
-- INSERT INTO `cloud`.`configuration` (category, instance, component, name, value, description) VALUES ('Advanced', 'DEFAULT', 'AgentManager', 'xenserver.public.network.device', 'public-network', "[OPTIONAL]The name of the XenServer network containing the physical network interface that is connected to the public network ");


