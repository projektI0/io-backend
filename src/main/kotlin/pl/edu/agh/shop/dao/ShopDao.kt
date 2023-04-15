package pl.edu.agh.shop.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.table.UserTable
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.table.ShopTable

object ShopDao {
    private fun userIdCondition(userId: LoginUserId): Op<Boolean> =
        ShopTable.generatedByUserId.isNull() or (ShopTable.generatedByUserId eq userId)

    fun getAllShops(limit: Int, offset: Long, userId: LoginUserId): List<ShopTableDTO> =
        ShopTable
            .select {
                userIdCondition(userId)
            }
            .limit(limit, offset = offset)
            .map { ShopTable.toDomain(it) }

    fun getAllShopsWithinBounds(lowerLeftLat: Double, lowerLeftLng: Double, upperRightLat: Double, upperRightLng: Double, userId: LoginUserId): List<ShopTableDTO> =
        ShopTable
            .select {
                userIdCondition(userId) and (ShopTable.latitude.between(lowerLeftLat, upperRightLat)) and (ShopTable.longitude.between(lowerLeftLng, upperRightLng))
            }
            .map {ShopTable.toDomain(it)}
    fun getShop(id: ShopId, userId: LoginUserId): Option<ShopTableDTO> =
        ShopTable
            .select{
                userIdCondition(userId) and (ShopTable.id eq id)
            }
            .firstOrNone()
            .map { ShopTable.toDomain(it) }
    fun insertNewShop(name: String, longitude: Double, latitude: Double, address: String, userId: LoginUserId): Option<ShopTableDTO> {
        val newShopId = ShopTable.insert {
            it[ShopTable.name] = name
            it[ShopTable.longitude] = longitude
            it[ShopTable.latitude] = latitude
            it[ShopTable.address] = address
            it[ShopTable.generatedByUserId] = userId
        } get ShopTable.id

        return getShop(newShopId, userId)
    }
}