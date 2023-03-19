package pl.edu.agh.shop.domain

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect

@Serializable
data class ShopId(val id: Int)

fun Table.shopId(name: String): Column<ShopId> = registerColumn(name, ShopIdColumnType)

object ShopIdColumnType : ColumnType() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.integerType()
    override fun valueFromDB(value: Any): ShopId {
        val intValue = when (value) {
            is Int -> (value)
            is Number -> (value.toInt())
            is String -> (value.toInt())
            else -> error("Unexpected value of type Int: $value of ${value::class.qualifiedName}")
        }
        return ShopId(intValue)
    }

    override fun valueToDB(value: Any?): Any? {
        if (value is ShopId) {
            return value.id
        }
        return null
    }
}
