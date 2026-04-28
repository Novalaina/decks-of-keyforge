
ALTER TABLE extra_card_info
    ADD COLUMN enhancement_power int4 NULL;
UPDATE extra_card_info
SET enhancement_power = 0;
ALTER TABLE extra_card_info
    ALTER COLUMN enhancement_power SET NOT NULL;

ALTER TYPE house ADD VALUE 'Ouboros';
