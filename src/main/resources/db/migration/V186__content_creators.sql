CREATE TABLE IF NOT EXISTS content_creator_details
(
    id                   UUID          NOT NULL,
    user_id              UUID          NOT NULL,

    content_name         VARCHAR(255)  NOT NULL,
    description          VARCHAR(2000) NOT NULL,
    link                 VARCHAR(500),
    discord_server       VARCHAR(500),
    promo_image_key      VARCHAR(500),
    content_creator_type VARCHAR(255)  NOT NULL,

    times_featured       INT4          NOT NULL DEFAULT 0,
    featured             BOOLEAN       NOT NULL DEFAULT FALSE,

    created              TIMESTAMP     NOT NULL,
    updated              TIMESTAMP     NOT NULL,

    PRIMARY KEY (id)
);

ALTER TABLE content_creator_details
    DROP CONSTRAINT IF EXISTS content_creator_details_user_fk;
ALTER TABLE content_creator_details
    ADD CONSTRAINT content_creator_details_user_fk FOREIGN KEY (user_id) REFERENCES key_user;

CREATE INDEX IF NOT EXISTS content_creator_details_user_id_idx ON content_creator_details (user_id);
CREATE INDEX IF NOT EXISTS content_creator_details_type_idx ON content_creator_details (content_creator_type);
CREATE INDEX IF NOT EXISTS content_creator_details_featured_idx ON content_creator_details (featured);
CREATE INDEX IF NOT EXISTS content_creator_details_updated_idx ON content_creator_details (updated);

CREATE TABLE IF NOT EXISTS content_creator_monthly_clicks
(
    id                 UUID        NOT NULL,
    content_creator_id UUID        NOT NULL,
    year_month         VARCHAR(7)  NOT NULL,
    clicks             INT4        NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
);

ALTER TABLE content_creator_monthly_clicks
    DROP CONSTRAINT IF EXISTS content_creator_monthly_clicks_fk;
ALTER TABLE content_creator_monthly_clicks
    ADD CONSTRAINT content_creator_monthly_clicks_fk FOREIGN KEY (content_creator_id) REFERENCES content_creator_details;

ALTER TABLE content_creator_monthly_clicks
    DROP CONSTRAINT IF EXISTS content_creator_monthly_clicks_uk;
ALTER TABLE content_creator_monthly_clicks
    ADD CONSTRAINT content_creator_monthly_clicks_uk UNIQUE (content_creator_id, year_month);

CREATE INDEX IF NOT EXISTS content_creator_monthly_clicks_year_month_idx ON content_creator_monthly_clicks (year_month);
