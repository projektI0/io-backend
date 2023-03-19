package pl.edu.agh.auth.domain

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect

@Serializable
data class LoginUserId(val id: Int)

fun Table.loginUserId(name: String): Column<LoginUserId> = registerColumn(name, LoginUserIdColumnType)

object LoginUserIdColumnType : ColumnType() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.integerType()
    override fun valueFromDB(value: Any): LoginUserId = when (value) {
        is Int -> LoginUserId(value)
        is Number -> LoginUserId(value.toInt())
        is String -> LoginUserId(value.toInt())
        else -> error("Unexpected value of type Int: $value of ${value::class.qualifiedName}")
    }

    override fun valueToDB(value: Any?): Any? {
        if(value is LoginUserId) {
            return value.id
        }
        return null
    }
}