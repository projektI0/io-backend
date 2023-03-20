package pl.edu.agh.shoppingList.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.productId
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.shoppingListId

object ShoppingListProductTable : Table("SHOPPING_LIST_PRODUCT") {
    val shoppingListId: Column<ShoppingListId> = shoppingListId("SHOPPING_LIST_ID")
    val productId: Column<ProductId> = productId("PRODUCT_ID")
    val quantity: Column<Double> = double("QUANTITY")
}