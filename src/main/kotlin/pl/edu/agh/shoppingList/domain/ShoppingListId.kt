package pl.edu.agh.shoppingList.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.utils.*

@Serializable(with = ShoppingListIdSerializer::class)
data class ShoppingListId(val int: Int) : GenId<ShoppingListId>(id = int)

object ShoppingListIdFactory : GenFactory<ShoppingListId>() {
    override fun create(id: Int): ShoppingListId = ShoppingListId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ShoppingListId::class)
private object ShoppingListIdSerializer : GenSerial<ShoppingListId>(ShoppingListIdFactory)

fun Table.shoppingListId(name: String): Column<ShoppingListId> = genericIntId(ShoppingListIdFactory)(name)
