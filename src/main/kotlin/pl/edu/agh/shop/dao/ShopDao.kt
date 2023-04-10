package pl.edu.agh.shop.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.shop.table.ShopTable
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
        name: String,
        longitude: Double,
        latitude: Double,
        address: String,
        userId: LoginUserId
    ): Option<ShopTableDTO> {
        logger.info("Adding new shop: $name")
        val newShopId = ShopTable.insert {
            it[ShopTable.name] = name
            it[ShopTable.longitude] = longitude
            it[ShopTable.latitude] = latitude
            it[ShopTable.address] = address
            it[generatedByUserId] = userId
        }[ShopTable.id]

        logger.info("Shop has been added with id $newShopId ($name)")

        return getShop(newShopId, userId)
    }
}
