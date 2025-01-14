\connect parts parts

-- load data from CSVs.
\copy product (product_id, product_name, product_category_name) FROM '/docker-entrypoint-initdb.d/products.csv' WITH (FORMAT csv, HEADER true)
\copy part (part_number, part_description, product_id, part_original_retail_price, part_brand_name, part_image_url) FROM '/docker-entrypoint-initdb.d/parts.csv' WITH (FORMAT csv, HEADER true)

-- Fix the sequences so that new inserts would get the next value.
SELECT setval('product_product_id_seq', (SELECT MAX(product_id) FROM product));
SELECT setval('part_part_id_seq', (SELECT MAX(part_id) FROM part));
