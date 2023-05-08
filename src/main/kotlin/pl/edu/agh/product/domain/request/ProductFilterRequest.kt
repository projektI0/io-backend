package pl.edu.agh.product.domain.request

import kotlinx.serialization.Serializable

/**
 * Request for getting filtered out products
 */
@Serializable
data class ProductFilterRequest(val names: List<String>, val limit: Int, val offset: Long)
