package pl.edu.agh.shoppingList.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.shoppingList.domain.dto.ShoppingListDTO

/**
 * Shopping list with its metadata and products
 */
@Serializable
data class ShoppingListView(val shoppingListDTO: ShoppingListDTO, val products: List<ShoppingListProductView>)
