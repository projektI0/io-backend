package pl.edu.agh.product.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.tag.domain.TagId

@Serializable
data class ProductView(val id: ProductId, val name: String, val description: String, val tags: List<TagId>)
