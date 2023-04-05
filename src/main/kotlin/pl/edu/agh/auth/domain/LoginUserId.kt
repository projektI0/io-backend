package pl.edu.agh.auth.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.utils.*

@Serializable(with = LoginUserIdSerializer::class)
data class LoginUserId(val int: Int) : GenId<LoginUserId>(id = int)

private object LoginUserIdFactory : GenFactory<LoginUserId>() {
    override fun create(id: Int): LoginUserId = LoginUserId(id)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LoginUserId::class)
object LoginUserIdSerializer : GenSerial<LoginUserId>(LoginUserIdFactory)

fun Table.loginUserId(name: String): Column<LoginUserId> = genericIntId(LoginUserIdFactory)(name)