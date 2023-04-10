package pl.edu.agh.shop.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class ShopRequest(val name: String, val longitude: Double, val latitude: Double, val address: String)
