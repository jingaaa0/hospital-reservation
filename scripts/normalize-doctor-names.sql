UPDATE doctors
SET name = REGEXP_REPLACE(name, '\s+원장$', '')
WHERE name ~ '\s+원장$';
