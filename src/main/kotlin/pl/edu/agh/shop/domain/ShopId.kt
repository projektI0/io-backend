package pl.edu.agh.shop.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.utils.GenericIntId
import pl.edu.agh.utils.GenericIntIdFactory
import pl.edu.agh.utils.GenericIntIdSerializer
import pl.edu.agh.utils.genericIntId

@Serializable(with = ShopIdSerializer::class)
data class ShopId(override val id: Int) : GenericIntId<ShopId>()

object ShopIdFactory : GenericIntIdFactory<ShopId>() {
    override fun create(id: Int): ShopId = ShopId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ShopId::class)
object ShopIdSerializer : GenericIntIdSerializer<ShopId>(ShopIdFactory) {
    override fun deserialize(decoder: Decoder): ShopId = super.deserialize(decoder)
    override fun serialize(encoder: Encoder, value: ShopId) = super.serialize(encoder, value)
}

fun Table.shopId(name: String): Column<ShopId> = genericIntId(ShopIdFactory)(name)
