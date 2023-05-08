package pl.edu.agh.shoppingList.dao

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.table.ProductTable
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListProductView
import pl.edu.agh.shoppingList.domain.dto.ShoppingListProductDTO
import pl.edu.agh.shoppingList.table.ShoppingListProductTable
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.utils.Transactor

object ShoppingListProductDao {
    suspend fun getAllShoppingListProducts(id: ShoppingListId): List<ShoppingListProductDTO> = Transactor.dbQuery {
        ShoppingListProductTable.select { ShoppingListProductTable.shoppingListId eq id }
            .map { ShoppingListProductTable.toDomain(it) }
    }

    fun addProductToShoppingList(shoppingListProduct: ShoppingListProductDTO) {
        ShoppingListProductTable.insert {
            it[shoppingListId] = shoppingListProduct.shoppingListId
            it[productId] = shoppingListProduct.productId
            it[quantity] = shoppingListProduct.quantity
        }
    }

    fun updateProductInShoppingList(shoppingListProduct: ShoppingListProductDTO) {
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

    suspend fun getAllTopTagsForList(id: ShoppingListId): MutableSet<TagId> {
        return Transactor.dbQuery {
            ProductTagTable
                .select(ProductTagTable.productId.inList(getAllShoppingListProducts(id).map { it.productId }))
                .map { it[ProductTagTable.mainTagId] }.toMutableSet()
        }
    }
}
