package pl.edu.agh.shoppingList.domain.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.shoppingList.domain.ShoppingListId

@Serializable
data class ShoppingListProductDTO(val shoppingListId: ShoppingListId, val productId: ProductId, val quantity: Double, val crossedOut: Boolean = false)
