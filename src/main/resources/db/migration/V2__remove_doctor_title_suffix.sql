UPDATE doctors
SET name = regexp_replace(name, '[[:space:]]+원장$', '')
WHERE name ~ '[[:space:]]+원장$';
