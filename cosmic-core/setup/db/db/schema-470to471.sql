--;
-- Schema upgrade from 4.7.0 to 4.7.1;
--;
ALTER TABLE cloud.s2s_customer_gateway ADD COLUMN force_encap INT(1) NOT NULL DEFAULT 0 AFTER dpd;
