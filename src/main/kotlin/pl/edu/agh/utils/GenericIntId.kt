package pl.edu.agh.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.Table

typealias GenId<T> = GenericIntId<T>
typealias GenFactory<T> = GenericIntIdFactory<T>
typealias GenSerial<T> = GenericIntIdSerializer<T>

@Serializable(with = GenSerial::class)
open class GenericIntId<T>(val id: Int) : Comparable<GenId<T>> {
    override fun compareTo(other: GenId<T>): Int {
        return id.compareTo(other.id)
    }
}

abstract class GenericIntIdFactory<T : GenId<T>> {
    abstract fun create(id: Int): T
}

abstract class GenericIntIdSerializer<T : GenId<T>>(private val factory: GenFactory<T>) : KSerializer<T> {

    override fun deserialize(decoder: Decoder): T = factory.create(decoder.decodeInt())

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeInt(value.id)
    }
}

class GenericIntIdColumnType<T : GenId<T>>(private val factory: GenFactory<T>) : ColumnType() {
    override var nullable: Boolean = false

    override fun sqlType(): String = IntegerColumnType().sqlType()

    override fun valueFromDB(value: Any): T {
        return when (value) {
            is Int -> factory.create(value)
            is Number -> factory.create(value.toInt())
            is String -> factory.create(value.toInt())
            else -> error("Unexpected value of type Int: $value of ${value::class.qualifiedName}")
        }
    }

    override fun valueToDB(value: Any?): Any? {
        if (value is GenericIntId<*>) {
            return value.id
        }
        return null
    }

}

fun <T : GenId<T>> Table.genericIntId(factory: GenFactory<T>): (String) -> Column<T> = {
    registerColumn(it, GenericIntIdColumnType(factory))
}