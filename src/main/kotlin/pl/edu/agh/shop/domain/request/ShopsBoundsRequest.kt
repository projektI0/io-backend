package pl.edu.agh.shop.domain.request

import kotlinx.serialization.Serializable

/**
 * Request to fetch all shops from given area
 */
@Serializable
data class ShopsBoundsRequest(
    val lowerLeftLat: Double,
    val lowerLeftLng: Double,
    val upperRightLat: Double,
    val upperRightLng: Double
)
