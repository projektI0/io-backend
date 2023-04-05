package pl.edu.agh.shoppingList.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.product.domain.ProductView

@Serializable
data class ShoppingListProductView(val product: ProductView, val quantity: Double)
