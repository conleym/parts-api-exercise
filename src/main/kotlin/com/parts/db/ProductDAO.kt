package com.parts.db

import com.parts.Loggers.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.set
import org.springframework.stereotype.Component

/**
 * Reads products and their parts from the database.
 */
@Component
class ProductDAO @Autowired constructor(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) {
    /**
     * Get all the products, one page at a time. Products are ordered by id. Each product's parts are also ordered by
     * id.
     *
     * @param lastId if non-null, only products with an id greater than this will be returned.
     * @param pageSize maximum number of products to return.
     */
    fun getAllProducts(lastId: Int?, pageSize: Int): List<ProductEntity> {
        val params = MapSqlParameterSource()
        params[GetAllProducts.LAST_ID_PARAM] = lastId
        params[GetAllProducts.PAGE_SIZE_PARAM] = pageSize
        LOGGER.debug("Getting all products with query params: {}", params)
        return jdbcTemplate.query(GetAllProducts.ALL_PRODUCTS_QUERY, params, GetAllProducts.ALL_PRODUCTS_RSE)
            ?: emptyList()
    }

    /**
     * Container for anything related to [getAllProducts].
     */
    private object GetAllProducts {
        const val PAGE_SIZE_PARAM = "pageSize"
        const val LAST_ID_PARAM = "lastId"

        /**
         * The query to get all products.
         *
         * [ALL_PRODUCTS_RSE] assumes the results are ordered by product.
         *
         * Ordering by part_id isn't strictly necessary, but ensures consistent query results. We could replace this
         * with a sorted set or simply accept unordered results as an alternative if performance were a concern.
         */
        const val ALL_PRODUCTS_QUERY = """
            WITH
            paged_product AS (
                SELECT *
                FROM product
                WHERE
                    CASE WHEN :${LAST_ID_PARAM}::int IS NULL THEN
                        TRUE
                    ELSE
                        product_id > :${LAST_ID_PARAM}
                    END
                ORDER BY product_id
                LIMIT :${PAGE_SIZE_PARAM}
            )
            SELECT *
            FROM paged_product INNER JOIN part USING(product_id)
            ORDER BY product_id, part_id
        """

        /**
         * Turns the result set produced by [ALL_PRODUCTS_QUERY] into entity objects.
         */
        val ALL_PRODUCTS_RSE = ResultSetExtractor<List<ProductEntity>> { rs ->
            // I don't love nested functions, but they do seem to make sense here.
            fun readPart() = PartEntity(
                partNumber = rs.getString("part_number"),
                description = rs.getString("part_description"),
                originalRetailPrice = rs.getBigDecimal("part_original_retail_price"),
                brandName = rs.getString("part_brand_name"),
                imageURL = rs.getString("part_image_url"),
            )

            fun readProductId() = rs.getInt("product_id")

            // Explicit types on these to avoid platform type warnings.
            fun readProductName(): String = rs.getString("product_name")
            fun readProductCategoryName(): String = rs.getString("product_category_name")

            // Each row contains part and product info. We'll loop through the rows, attaching parts to products,
            // relying on the fact that the rows are ordered by product.
            if (!rs.next()) {
                emptyList()
            } else {
                // Can assume nonempty result set here, so we're free to start reading.
                val result = mutableListOf<ProductEntity>()
                var currentParts = mutableListOf<PartEntity>()
                var currentProductId = readProductId()
                var currentProductName = readProductName()
                var currentProductCategoryName = readProductCategoryName()

                // Kind of gross, but saves us copy/pasting this twice.
                fun createProduct() = ProductEntity(
                    id = currentProductId,
                    name = currentProductName,
                    categoryName = currentProductCategoryName,
                    parts = currentParts,
                )

                do {
                    val productId = readProductId()
                    if (productId != currentProductId) {
                        // Finish existing product, start new product, add new part to it.
                        val productEntity = createProduct()
                        LOGGER.trace("Finalized product id {}: {}", currentProductId, productEntity)
                        result.add(productEntity)

                        currentParts = mutableListOf(readPart())
                        currentProductId = productId
                        currentProductName = readProductName()
                        currentProductCategoryName = readProductCategoryName()
                    } else { // Add new part to list.
                        currentParts.add(readPart())
                    }
                } while (rs.next())

                // Add the last product, which wasn't added above. It's pretty easy to see this -- consider the case
                // where there is exactly one product in the results. The check for a new product ID will always be
                // false when looping through the rows.
                val productEntity = createProduct()
                LOGGER.trace("Finalized product id {}: {}", currentProductId, productEntity)
                result.add(productEntity)

                result
            }
        }
    }

    private companion object {
        private val LOGGER = getLogger<ProductDAO>()
    }
}
