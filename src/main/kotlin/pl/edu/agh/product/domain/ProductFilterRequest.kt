package pl.edu.agh.product.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProductFilterRequest(val names: List<String>, val limit: Int, val offset: Long)
