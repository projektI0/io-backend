package pl.edu.agh.tag.domain

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

@Serializable(with = TagIdSerializer::class)
data class TagId(override val id: Int) : GenericIntId<TagId>()

object TagIdFactory : GenericIntIdFactory<TagId>() {
    override fun create(id: Int): TagId = TagId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = TagId::class)
object TagIdSerializer : GenericIntIdSerializer<TagId>(TagIdFactory) {
    override fun deserialize(decoder: Decoder): TagId = super.deserialize(decoder)
    override fun serialize(encoder: Encoder, value: TagId) = super.serialize(encoder, value)
}

fun Table.tagId(name: String): Column<TagId> = genericIntId(TagIdFactory)(name)
