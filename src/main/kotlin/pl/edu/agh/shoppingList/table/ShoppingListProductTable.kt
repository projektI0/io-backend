package pl.edu.agh.shoppingList.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.productId
import pl.edu.agh.product.table.ProductTable
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListProductView
import pl.edu.agh.shoppingList.domain.dto.ShoppingListProductDTO
import pl.edu.agh.shoppingList.domain.shoppingListId

object ShoppingListProductTable : Table("SHOPPING_LIST_PRODUCT") {
    val shoppingListId: Column<ShoppingListId> = shoppingListId("SHOPPING_LIST_ID")
    val productId: Column<ProductId> = productId("PRODUCT_ID")
    val quantity: Column<Double> = double("QUANTITY")
    val crossedOut: Column<Boolean> = bool("CROSSED_OUT").default(false)

    fun toDomain(it: ResultRow): ShoppingListProductDTO = ShoppingListProductDTO(
        shoppingListId = it[shoppingListId],
        productId = it[productId],
        quantity = it[quantity],
        crossedOut = it[crossedOut]
    )

    fun toDomainView(it: ResultRow): ShoppingListProductView =
        ShoppingListProductView(
            product = ProductTable.toDomainView(ProductTable.select { ProductTable.id eq it[productId] }.first()),
            quantity = it[quantity],
            crossedOut = it[crossedOut],
        )
}
