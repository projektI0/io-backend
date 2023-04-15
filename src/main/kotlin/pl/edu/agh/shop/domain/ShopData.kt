package pl.edu.agh.shop.domain

import kotlinx.serialization.Serializable

@Serializable
class ShopData(val name: String, val longitude: Double, val latitude: Double, val address: String)