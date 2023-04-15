package pl.edu.agh.product.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProductTableDTO(val id: ProductId, val name: String, val description: String)