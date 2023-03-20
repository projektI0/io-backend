package pl.edu.agh.shop.dao

import org.jetbrains.exposed.sql.selectAll
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.table.ShopTable

object ShopDao {
    fun getSimpleShops(limit: Int, offset: Long = 0): List<ShopTableDTO> =
        ShopTable
            .selectAll()
            .limit(limit, offset = offset)
            .map { ShopTable.toDomain(it) }
}