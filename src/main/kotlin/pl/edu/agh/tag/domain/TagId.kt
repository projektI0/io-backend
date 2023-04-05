package pl.edu.agh.tag.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.utils.*

@Serializable(with = TagIdSerializer::class)
data class TagId(val int: Int) : GenId<TagId>(id = int)

private object TagIdFactory : GenFactory<TagId>() {
    override fun create(id: Int): TagId = TagId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = TagId::class)
object TagIdSerializer : GenSerial<TagId>(TagIdFactory)

fun Table.tagId(name: String): Column<TagId> = genericIntId(TagIdFactory)(name)
