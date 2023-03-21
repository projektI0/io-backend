package pl.edu.agh.shop.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.domain.shopId

object ShopTable : IdTable<ShopId>("SHOP") {
    override val id: Column<EntityID<ShopId>> = shopId("ID").autoIncrement().entityId()
    val name: Column<String> = varchar("NAME", 256)
    val longitude: Column<Double> = double("LONGITUDE")
    val latitude: Column<Double> = double("LATITUDE")
    val address: Column<String> = varchar("ADDRESS", 256)

    fun toDomain(it: ResultRow): ShopTableDTO = ShopTableDTO(
        id = it[id].value, name = it[name], longitude = it[longitude], latitude = it[latitude], address = it[address]
    )
}