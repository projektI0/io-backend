package pl.edu.agh.shoppingList.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.utils.InstantSerializer
import java.time.Instant

@Serializable
data class ShoppingListView(
    val shoppingList: ShoppingList,
    val products: List<ShoppingListProductView>
)
