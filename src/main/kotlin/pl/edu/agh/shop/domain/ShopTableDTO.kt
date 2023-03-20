package pl.edu.agh.shop.domain

import kotlinx.serialization.Serializable

@Serializable
data class ShopTableDTO(
    val id: ShopId,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val address: String
)
