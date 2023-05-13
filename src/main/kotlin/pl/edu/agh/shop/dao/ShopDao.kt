package pl.edu.agh.shop.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.shop.domain.request.ShopRequest
import pl.edu.agh.shop.table.ShopTable
import pl.edu.agh.shop.table.ShopTagTable
import pl.edu.agh.tag.table.TagTable
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Transactor

object ShopDao {
    private val logger by LoggerDelegate()

    private fun userIdCondition(userId: LoginUserId): Op<Boolean> =
        ShopTable.generatedByUserId.isNull() or (ShopTable.generatedByUserId eq userId)

    fun getAllShops(limit: Int, offset: Long, userId: LoginUserId): List<ShopTableDTO> =
        ShopTable
            .select {
                userIdCondition(userId)
            }
            .limit(limit, offset = offset)
            .map { ShopTable.toDomain(it) }

    suspend fun getAllShopsWithinBounds(
        lowerLeftLat: Double,
        lowerLeftLng: Double,
        upperRightLat: Double,
        upperRightLng: Double,
        userId: LoginUserId
    ): List<ShopTableDTO> =
        Transactor.dbQuery {
            ShopTable
                .select {
                    userIdCondition(userId) and
                        ShopTable.latitude.between(lowerLeftLat, upperRightLat) and
                        ShopTable.longitude.between(lowerLeftLng, upperRightLng)
                }
                .map { ShopTable.toDomain(it) }
        }

    fun getShop(id: ShopId, userId: LoginUserId): Option<ShopTableDTO> =
        ShopTable
            .select {
                userIdCondition(userId) and (ShopTable.id eq id)
            }
            .firstOrNone()
            .map { ShopTable.toDomain(it) }

    fun insertNewShop(
        shopRequest: ShopRequest,
        userId: LoginUserId
    ): Option<ShopTableDTO> {
        logger.info("Adding new shop: ${shopRequest.name}")
        val newShopId = ShopTable.insert {
            it[ShopTable.name] = shopRequest.name
            it[ShopTable.longitude] = shopRequest.longitude
            it[ShopTable.latitude] = shopRequest.latitude
            it[ShopTable.address] = shopRequest.address
            it[ShopTable.generatedByUserId] = userId
        }[ShopTable.id]

        ShopTagTable.batchInsert(
            TagTable.select { TagTable.id.inList(shopRequest.tags) }
                .map { it[TagTable.parentTagId] ?: it[TagTable.id] }
                .distinct()
        ) {
            this[ShopTagTable.shopId] = newShopId
            this[ShopTagTable.tagId] = it
        }

        logger.info("Shop has been added with id $newShopId ${shopRequest.name}")

        return getShop(newShopId, userId)
    }
}
