package pl.edu.agh.product.domain

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

@Serializable(ProductIdSerializer::class)
data class ProductId(override val id: Int) : GenericIntId<ProductId>()

object ProductIdFactory : GenericIntIdFactory<ProductId>() {
    override fun create(id: Int): ProductId = ProductId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ProductId::class)
object ProductIdSerializer : GenericIntIdSerializer<ProductId>(ProductIdFactory) {

    override fun deserialize(decoder: Decoder): ProductId = super.deserialize(decoder)

    override fun serialize(encoder: Encoder, value: ProductId) = super.serialize(encoder, value)
}

fun Table.productId(name: String): Column<ProductId> = genericIntId(ProductIdFactory)(name)
