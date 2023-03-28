package pl.edu.agh.shop.domain

import kotlinx.serialization.Serializable

@Serializable
data class ShopsBoundsRequest(
    val lowerLeftLat:  Double,
    val lowerLeftLng:  Double,
    val upperRightLat: Double,
    val upperRightLng: Double,
)
