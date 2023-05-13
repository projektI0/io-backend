package pl.edu.agh.product.domain.request

import kotlinx.serialization.Serializable
import pl.edu.agh.tag.domain.TagId

/**
 * Request for adding, new customer's product
 */
@Serializable
data class ProductRequest(val name: String, val description: String, val tags: List<TagId>)
