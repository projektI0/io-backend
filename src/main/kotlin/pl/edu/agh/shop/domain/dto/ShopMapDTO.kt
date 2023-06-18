package pl.edu.agh.shop.domain.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.shop.domain.ShopId

@Serializable
data class ShopMapDTO(
    val id: ShopId,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val address: String,
    val user: Boolean,
    val blacklist: Boolean
)
