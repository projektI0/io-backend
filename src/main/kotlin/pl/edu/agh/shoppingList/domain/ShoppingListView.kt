package pl.edu.agh.shoppingList.domain

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListView(val shoppingList: ShoppingList, val products: List<ShoppingListProductView>)
