package pl.edu.agh.shoppingList.domain

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

@Serializable(with = ShoppingListIdSerializer::class)
data class ShoppingListId(override val id: Int) : GenericIntId<ShoppingListId>()

private object ShoppingListIdFactory : GenericIntIdFactory<ShoppingListId>() {
    override fun create(id: Int): ShoppingListId = ShoppingListId(id)
}

@Serializer(forClass = ShoppingListId::class)
private object ShoppingListIdSerializer : GenericIntIdSerializer<ShoppingListId>(ShoppingListIdFactory) {
    override fun deserialize(decoder: Decoder): ShoppingListId = super.deserialize(decoder)
    override fun serialize(encoder: Encoder, value: ShoppingListId) = super.serialize(encoder, value)
}


fun Table.shoppingListId(name: String): Column<ShoppingListId> = genericIntId(ShoppingListIdFactory)(name)
