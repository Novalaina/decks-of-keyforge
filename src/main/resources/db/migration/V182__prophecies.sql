ALTER TYPE HOUSE ADD VALUE IF NOT EXISTS 'Prophecy';

ALTER TABLE extra_card_info
    DROP COLUMN IF EXISTS enhancement_brobnar;
ALTER TABLE extra_card_info
    DROP COLUMN IF EXISTS enhancement_dis;
ALTER TABLE extra_card_info
    DROP COLUMN IF EXISTS enhancement_ekwidon;
ALTER TABLE extra_card_info
    DROP COLUMN IF EXISTS enhancement_geistoid;
ALTER TABLE extra_card_info
    DROP COLUMN IF EXISTS enhancement_logos;
ALTER TABLE extra_card_info
    DROP COLUMN IF EXISTS enhancement_mars;
ALTER TABLE extra_card_info
    DROP COLUMN IF EXISTS enhancement_skyborn;

ALTER TABLE deck
    ALTER COLUMN bonus_icons_string TYPE TEXT;
ALTER TABLE theoretical_deck
    ALTER COLUMN bonus_icons_string TYPE TEXT;
ALTER TABLE alliance_deck
    ALTER COLUMN bonus_icons_string TYPE TEXT;
