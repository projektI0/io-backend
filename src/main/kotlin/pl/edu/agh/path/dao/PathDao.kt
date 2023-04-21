package pl.edu.agh.path.dao

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.select
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.shop.table.ShopTable
import pl.edu.agh.shop.table.ShopTagTable
import pl.edu.agh.shoppingList.domain.dto.ShoppingListProductDTO
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.utils.Transactor

object PathDao {
    fun getTagsForShops(shops: Set<ShopId>): Map<ShopTableDTO, Set<TagId>> =
        ShopTagTable
            .join(ShopTable, JoinType.INNER, ShopTagTable.shopId, ShopTable.id)
            .select(ShopTagTable.shopId.inList(shops))
            .map { ShopTable.toDomain(it) to it[ShopTagTable.tagId] }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

    fun getShopsForTags(shops: Set<ShopId>, tags: Set<TagId>): Map<TagId, Set<ShopTableDTO>> =
        ShopTagTable
            .join(ShopTable, JoinType.INNER, ShopTagTable.shopId, ShopTable.id)
            .select(ShopTagTable.tagId.inList(tags))
            .map { it[ShopTagTable.tagId] to ShopTable.toDomain(it) }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

    suspend fun getTagsForProducts(products: List<ShoppingListProductDTO>): MutableSet<TagId> {
        return Transactor.dbQuery {
            ProductTagTable.select(ProductTagTable.productId.inList(products.map { it.productId }))
                .map { it[ProductTagTable.tagId] }.toMutableSet()
        }
    }
}
