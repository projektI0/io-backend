package pl.edu.agh.path.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.shoppingList.domain.ShoppingListId

/**
 * Request for calculating path for shopping list
 */
@Serializable
data class PathRequest(val shoppingListId: ShoppingListId, val longitude: Double, val latitude: Double, val fewestShops: Boolean?)
