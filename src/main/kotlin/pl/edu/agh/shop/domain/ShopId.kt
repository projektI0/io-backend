package pl.edu.agh.shop.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.utils.*

@Serializable(with = ShopIdSerializer::class)
data class ShopId(val int: Int) : GenId<ShopId>(id = int)

private object ShopIdFactory : GenFactory<ShopId>() {
    override fun create(id: Int): ShopId = ShopId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ShopId::class)
private object ShopIdSerializer : GenSerial<ShopId>(ShopIdFactory)

fun Table.shopId(name: String): Column<ShopId> = genericIntId(ShopIdFactory)(name)