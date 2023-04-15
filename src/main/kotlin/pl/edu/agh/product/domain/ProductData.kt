package pl.edu.agh.product.domain

import kotlinx.serialization.Serializable

@Serializable
class ProductData(val name: String, val description: String)