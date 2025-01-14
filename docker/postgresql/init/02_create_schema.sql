\connect parts parts

CREATE TABLE product (
    product_id BIGSERIAL PRIMARY KEY,
    product_name TEXT NOT NULL,
    -- reasonable candidate to be an enum here.
    -- reasonable index candidate if we had category queries.
    product_category_name TEXT NOT NULL
);

CREATE TABLE part (
    part_id BIGSERIAL PRIMARY KEY,
    part_number TEXT NOT NULL,
    part_description TEXT NOT NULL,
    -- 5 total digits, 2 following the decimal.
    -- seems adequate for the data we're loading.
    part_original_retail_price NUMERIC(5, 2) NOT NULL,
    product_id BIGINT NOT NULL REFERENCES product(product_id),
    part_brand_name TEXT NOT NULL,
    part_image_url TEXT NOT NULL
);

-- if we had any queries that were improved by indices, they would go here.

-- postgres doesn't make indices on foreign key columns unless you tell it to. likely to be useful in general, but not
-- helpful for our queries here.
-- CREATE INDEX part_product_id_fkey_index ON part(product_id);
