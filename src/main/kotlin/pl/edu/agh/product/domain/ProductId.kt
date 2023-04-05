package pl.edu.agh.product.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.utils.*

@Serializable(ProductIdSerializer::class)
data class ProductId(val int: Int) : GenId<ProductId>(id = int)

private object ProductIdFactory : GenFactory<ProductId>() {
    override fun create(id: Int): ProductId = ProductId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ProductId::class)
object ProductIdSerializer : GenSerial<ProductId>(ProductIdFactory)

fun Table.productId(name: String): Column<ProductId> = genericIntId(ProductIdFactory)(name)