ALTER TABLE doctors
    ALTER COLUMN active DROP DEFAULT;

ALTER TABLE doctors
    ALTER COLUMN active TYPE VARCHAR(1)
    USING CASE WHEN active THEN 'Y' ELSE 'N' END;

ALTER TABLE doctors
    ALTER COLUMN active SET DEFAULT 'Y';

ALTER TABLE doctors
    ADD CONSTRAINT ck_doctors_active
    CHECK (active IN ('Y', 'L', 'N'));
