package pl.edu.agh.product.domain.request

import kotlinx.serialization.Serializable
import pl.edu.agh.tag.domain.TagId

@Serializable
data class ProductFilterRequest(val query: String, val tags: List<TagId>)
