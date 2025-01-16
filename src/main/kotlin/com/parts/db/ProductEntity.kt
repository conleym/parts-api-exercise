package com.parts.db

/**
 * A product, including constituent parts.
 */
data class ProductEntity(
    val id: Int,
    val name: String,
    val categoryName: String,
    val parts: List<PartEntity>, // sorted by id, just for consistency.
)

