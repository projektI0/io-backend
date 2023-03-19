package pl.edu.agh.shop.db

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.shopId

object ShopTable {
    @Serializable
    data class ShopTableDTO(
        val id: ShopId,
        val name: String,
        val longitude: Double,
        val latitude: Double,
        val address: String
    )

    object ShopTable : Table("SHOP") {
        val id: Column<ShopId> = shopId("ID").autoIncrement()
        val name: Column<String> = varchar("NAME", 256)
        val longitude: Column<Double> = double("LONGITUDE")
        val latitude: Column<Double> = double("LATITUDE")
        val address: Column<String> = varchar("ADDRESS", 256)
        override val primaryKey = PrimaryKey(id)

        fun toDomain(it: ResultRow): ShopTableDTO =
            ShopTableDTO(
                id = it[id], name = it[name], longitude = it[longitude], latitude = it[latitude], address = it[address]
            )
    }

    fun getSimpleShops(limit: Int, offset: Long = 0): List<ShopTableDTO> {
        return ShopTable.selectAll().limit(limit, offset = offset).map { ShopTable.toDomain(it) }
    }

}
