package pl.edu.agh.product.domain.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.tag.domain.TagId

@Serializable
data class ProductTagTableDTO(val product: ProductTableDTO, val tags: Set<TagId>)
