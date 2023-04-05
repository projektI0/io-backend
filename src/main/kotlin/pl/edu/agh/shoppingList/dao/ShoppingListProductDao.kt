package pl.edu.agh.shoppingList.dao

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.table.ProductTable
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListProduct
import pl.edu.agh.shoppingList.domain.ShoppingListProductView
import pl.edu.agh.shoppingList.table.ShoppingListProductTable

object ShoppingListProductDao {
    fun getAllShoppingListProducts(id: ShoppingListId): List<ShoppingListProduct> {
        return ShoppingListProductTable.select { ShoppingListProductTable.shoppingListId eq id }
            .map { ShoppingListProductTable.toDomain(it) }
    }

    fun addProductToShoppingList(shoppingListProduct: ShoppingListProduct) {
        ShoppingListProductTable.insert {
            it[shoppingListId] = shoppingListProduct.shoppingListId
            it[productId] = shoppingListProduct.productId
            it[quantity] = shoppingListProduct.quantity
        }
    }

    fun updateProductInShoppingList(shoppingListProduct: ShoppingListProduct) {
        ShoppingListProductTable.update({
            (ShoppingListProductTable.shoppingListId eq shoppingListProduct.shoppingListId) and (ShoppingListProductTable.productId eq shoppingListProduct.productId)
        }) {
            it[quantity] = shoppingListProduct.quantity
        }
    }

    fun deleteProductFromShoppingList(shoppingListId: ShoppingListId, productId: ProductId) {
        ShoppingListProductTable.deleteWhere {
            (ShoppingListProductTable.shoppingListId eq shoppingListId) and (ShoppingListProductTable.productId eq productId)
        }
    }

    fun getAllShoppingListProductsView(shoppingListId: ShoppingListId): List<ShoppingListProductView> =
        ShoppingListProductTable.join(ProductTable, JoinType.INNER, ShoppingListProductTable.productId, ProductTable.id)
            .select(ShoppingListProductTable.shoppingListId eq shoppingListId)
            .map { ShoppingListProductTable.toDomainView(it) }
}
