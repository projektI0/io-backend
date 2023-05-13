package pl.edu.agh.shoppingList.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.product.domain.dto.ProductTagTableDTO

/**
 * Single product on shopping list with its quantity
 */
@Serializable
data class ShoppingListProductView(val product: ProductTagTableDTO, val quantity: Double, val crossedOut: Boolean)
