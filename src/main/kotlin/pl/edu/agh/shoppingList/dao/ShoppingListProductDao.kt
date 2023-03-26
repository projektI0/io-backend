package pl.edu.agh.shoppingList.dao

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    fun addProductToShoppingList(shoppingListProduct: ShoppingListProduct): Unit {
        ShoppingListProductTable.insert {
            it[shoppingListId] = shoppingListProduct.shoppingListId
            it[productId] = shoppingListProduct.productId
            it[quantity] = shoppingListProduct.quantity
        }
    }

    fun updateProductInShoppingList(shoppingListProduct: ShoppingListProduct): Unit {
        ShoppingListProductTable.update({
                (ShoppingListProductTable.shoppingListId eq shoppingListProduct.shoppingListId) and (ShoppingListProductTable.productId eq shoppingListProduct.productId)
            }) {
                it[quantity] = shoppingListProduct.quantity
            }
    }

    fun deleteProductFromShoppingList(shoppingListId: ShoppingListId, productId: ProductId): Unit {
        ShoppingListProductTable.deleteWhere {
                (ShoppingListProductTable.shoppingListId eq shoppingListId) and (ShoppingListProductTable.productId eq productId)
            }
    }

    fun getAllShoppingListProductsView(shoppingListId: ShoppingListId): List<ShoppingListProductView> =
        ShoppingListProductTable.join(ProductTable, JoinType.INNER, ShoppingListProductTable.productId, ProductTable.id)
            .select(ShoppingListProductTable.shoppingListId eq shoppingListId)
            .map { ShoppingListProductTable.toDomainView(it) }
}