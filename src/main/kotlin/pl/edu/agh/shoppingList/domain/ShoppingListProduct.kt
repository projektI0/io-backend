package pl.edu.agh.shoppingList.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.product.domain.ProductId

@Serializable
data class ShoppingListProduct(val shoppingListId: ShoppingListId, val productId: ProductId, val quantity: Double)
