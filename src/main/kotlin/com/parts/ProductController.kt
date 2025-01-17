package com.parts

import com.parts.db.PartEntity
import com.parts.db.ProductDAO
import com.parts.db.ProductEntity
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/product")
class ProductController @Autowired constructor(
    private val productDAO: ProductDAO,
) {
    /**
     * Returns up to [pageSize] products (with their parts), in order by id.
     *
     * The [lastId] allows keyset paging by id. If provided, only results with id greater than [lastId] are returned.
     *
     * See [this article](https://use-the-index-luke.com/no-offset) for details on keyset pagination.
     */
    @GetMapping
    fun getAll(
        @RequestParam("last_id") lastId: Int?,
        @[
            Min(1)
            Max(MAX_PAGE_SIZE.toLong())
            RequestParam(name = "page_size", defaultValue = DEFAULT_PAGE_SIZE.toString())
        ] pageSize: Int
    ): List<Product> {
        val entities = productDAO.getAllProducts(lastId, pageSize)
        // Convert entities to API response objects.
        return entities.map { productEntity ->
            Product(
                id = productEntity.id,
                name = productEntity.name,
                categoryName = productEntity.categoryName,
                parts = productEntity.parts.map { partEntity ->
                    Part(
                        partNumber = partEntity.partNumber,
                        description = partEntity.description,
                        originalRetailPrice = partEntity.originalRetailPrice,
                        brandName = partEntity.brandName,
                        imageURL = partEntity.imageURL,
                    )
                },
            )
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 10
        private const val MAX_PAGE_SIZE = 100 // arbitrary, may need to be adjusted.
    }
}

/**
 * API view of a [PartEntity], visible to clients.
 */
data class Part(
    val partNumber: String,
    val description: String,
    val originalRetailPrice: BigDecimal,
    val brandName: String,
    val imageURL: String,
)

/**
 * API view of a [ProductEntity], visible to clients.
 */
data class Product(
    val id: Int,
    val name: String,
    val categoryName: String,
    val parts: List<Part>, // sorted by id, just for consistency.
)
