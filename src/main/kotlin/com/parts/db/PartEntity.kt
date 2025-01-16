package com.parts.db

import java.math.BigDecimal

/**
 * A single part.
 */
data class PartEntity(
    val partNumber: String,
    val description: String,
    val originalRetailPrice: BigDecimal,
    val brandName: String,
    val imageURL: String,
)
