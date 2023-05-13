package pl.edu.agh.shop.domain.request

import kotlinx.serialization.Serializable
import pl.edu.agh.tag.domain.TagId

@Serializable
data class ShopRequest(val name: String, val longitude: Double, val latitude: Double, val address: String, val tags: List<TagId>)
