package pl.edu.agh.path.dao

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.select
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.shop.table.ShopTable
import pl.edu.agh.shop.table.ShopTagTable
import pl.edu.agh.shoppingList.domain.dto.ShoppingListProductDTO
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.utils.Transactor

object PathDao {
    fun getTagsForShops(shops: Collection<ShopTableDTO>): Map<ShopTableDTO, Set<TagId>> {
        val map = mutableMapOf<ShopTableDTO, Set<TagId>>()
        shops.forEach { shop ->
            map[shop] = ShopTagTable.select(ShopTagTable.shopId.eq(shop.id)).map { it[ShopTagTable.tagId] }.toSet()
        }
        return map
    }

    fun getShopsForTags(shops: List<ShopTableDTO>, tags: Set<TagId>): Map<TagId, Set<ShopTableDTO>> {
        val map = mutableMapOf<TagId, Set<ShopTableDTO>>()
        tags.forEach { tag ->
            map[tag] = (ShopTagTable innerJoin ShopTable).select(ShopTagTable.tagId.eq(tag)).map {
                ShopTable.toDomain(it)
            }.filter { shopTableDTO -> shops.contains(shopTableDTO) }.toSet()
        }
        return map
    }

    suspend fun getTagsForProducts(products: List<ShoppingListProductDTO>): MutableSet<TagId> {
        return Transactor.dbQuery {
            ProductTagTable.select(ProductTagTable.productId.inList(products.map { it.productId }))
                .map { it[ProductTagTable.tagId] }.toMutableSet()
        }
    }
}
