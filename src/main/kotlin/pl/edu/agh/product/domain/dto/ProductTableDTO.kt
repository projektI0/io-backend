package pl.edu.agh.product.domain.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.product.domain.ProductId

@Serializable
data class ProductTableDTO(val id: ProductId, val name: String, val description: String)
