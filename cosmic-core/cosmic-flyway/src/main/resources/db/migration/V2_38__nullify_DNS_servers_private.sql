--- Nullify DNS servers for Private networks
UPDATE `networks` SET dns1=NULL, dns2=NULL WHERE guest_type = 'Private' AND removed is NULL;
