package pl.edu.agh.path.dao

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.shop.table.BlacklistShopTable
import pl.edu.agh.shop.table.ShopTable
import pl.edu.agh.shop.table.ShopTagTable
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.table.ShoppingListProductTable
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.utils.Transactor

object PathDao {
    fun getTagsForShops(shops: Set<ShopId>, userId: LoginUserId): Map<ShopTableDTO, Set<TagId>> =
        ShopTagTable
            .join(ShopTable, JoinType.INNER, ShopTagTable.shopId, ShopTable.id)
            .select(ShopTagTable.shopId.inList(shops))
            .map { ShopTable.toDomain(it, userId) to it[ShopTagTable.tagId] }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

    fun getShopsForTags(shops: Set<ShopId>, tags: Set<TagId>, userId: LoginUserId): Map<TagId, Set<ShopTableDTO>> =
        ShopTagTable
            .join(ShopTable, JoinType.INNER, ShopTagTable.shopId, ShopTable.id)
            .select(ShopTagTable.tagId.inList(tags) and ShopTagTable.shopId.inList(shops))
            .map { it[ShopTagTable.tagId] to ShopTable.toDomain(it, userId) }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

    suspend fun getAllTopTagsForList(id: ShoppingListId): MutableSet<TagId> {
        return Transactor.dbQuery {
            ProductTagTable
                .join(
                    ShoppingListProductTable,
                    JoinType.INNER,
                    ProductTagTable.productId,
                    ShoppingListProductTable.productId
                )
                .select { ShoppingListProductTable.shoppingListId eq id }
                .map { it[ProductTagTable.mainTagId] }.toMutableSet()
        }
    }

    private fun userIdCondition(userId: LoginUserId): Op<Boolean> =
        ShopTable.generatedByUserId.isNull() or (ShopTable.generatedByUserId eq userId)

    suspend fun getAllPossibleShopsWithinBounds(
        lowerLeftLat: Double,
        lowerLeftLng: Double,
        upperRightLat: Double,
        upperRightLng: Double,
        userId: LoginUserId
    ): List<ShopTableDTO> =
        Transactor.dbQuery {
            val blacklistedShopIds = BlacklistShopTable
                .select { BlacklistShopTable.userId eq userId }
                .map { it[BlacklistShopTable.shopId] }
            ShopTable.select {
                userIdCondition(userId) and
                        ShopTable.latitude.between(lowerLeftLat, upperRightLat) and
                        ShopTable.longitude.between(lowerLeftLng, upperRightLng) and
                        ShopTable.id.notInList(blacklistedShopIds)
            }
                .map { ShopTable.toDomain(it, userId) }
        }
}
