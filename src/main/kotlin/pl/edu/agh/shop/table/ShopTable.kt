package pl.edu.agh.shop.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.loginUserId
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.domain.shopId

object ShopTable : Table("SHOP") {
    val id: Column<ShopId> = shopId("ID").autoIncrement()
    val name: Column<String> = varchar("NAME", 256)
    val longitude: Column<Double> = double("LONGITUDE")
    val latitude: Column<Double> = double("LATITUDE")
    val address: Column<String> = varchar("ADDRESS", 256)
    val generatedByUserId: Column<LoginUserId?> = loginUserId("GENERATED_BY_USER_ID").nullable()

    fun toDomain(it: ResultRow): ShopTableDTO = ShopTableDTO(
        id = it[id],
        name = it[name],
        longitude = it[longitude],
        latitude = it[latitude],
        address = it[address]
    )
}
