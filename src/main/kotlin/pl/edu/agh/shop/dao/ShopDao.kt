package pl.edu.agh.shop.dao

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.table.ShopTable

object ShopDao {
    fun getAllShops(limit: Int = 100, offset: Long = 0): List<ShopTableDTO> =
        ShopTable
            .selectAll()
            .limit(limit, offset = offset)
            .map { ShopTable.toDomain(it) }

    fun getAllShopsWithinBounds(lowerLeftLat: Double, lowerLeftLng: Double, upperRightLat: Double, upperRightLng: Double ): List<ShopTableDTO> =
        ShopTable
            .select {
                (ShopTable.latitude.between(lowerLeftLat, upperRightLat)) and (ShopTable.longitude.between(lowerLeftLng, upperRightLng))
            }
            .map {ShopTable.toDomain(it)}
}