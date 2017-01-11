--;
-- Schema upgrade from 5.2.0 to 5.3.0;
--;
UPDATE networks SET guest_type='Private' WHERE name LIKE 'vpc-%-privateNetwork' and guest_type='Isolated';
