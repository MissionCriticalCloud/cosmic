--;
-- Schema upgrade from 4.5.0 to 4.5.1;
--;

UPDATE IGNORE `cloud`.`configuration` SET `default_value`='PBKDF2,SHA256SALT,MD5,LDAP,SAML2,PLAINTEXT' WHERE name='user.authenticators.order';
UPDATE IGNORE `cloud`.`configuration` SET `value`='PBKDF2,SHA256SALT,MD5,LDAP,SAML2,PLAINTEXT' WHERE name='user.authenticators.order';
UPDATE IGNORE `cloud`.`configuration` SET `default_value`='PBKDF2,SHA256SALT,MD5,LDAP,SAML2,PLAINTEXT' WHERE name='user.password.encoders.order';
UPDATE IGNORE `cloud`.`configuration` SET `value`='PBKDF2,SHA256SALT,MD5,LDAP,SAML2,PLAINTEXT' WHERE name='user.password.encoders.order';
UPDATE IGNORE `cloud`.`configuration` SET `value`="MD5,LDAP,PLAINTEXT" WHERE `name`="user.password.encoders.exclude";
ALTER TABLE `cloud`.`user` ADD COLUMN `source` varchar(40) NOT NULL DEFAULT 'UNKNOWN';
