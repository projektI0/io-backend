package pl.edu.agh.auth.db

import arrow.core.Option
import arrow.core.none
import arrow.core.singleOrNone
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.loginUserId

object UserTable {
    @Serializable
    data class UserTableDTO(val id: LoginUserId, val email: String)

    object UserTable : Table("LOGIN_USER") {
        val id: Column<LoginUserId> = loginUserId("ID").autoIncrement()
        val email: Column<String> = varchar("LOGIN", 256)
        val password: Column<String> = varchar("PASSWORD", 256)
        override val primaryKey = PrimaryKey(id)

        fun toDomain(it: ResultRow): Option<UserTableDTO> = Option.catch(recover = {
            println("Error while converting to domain: $it")
            none()
        }) {
            UserTableDTO(
                id = it[id], email = it[email]
            )
        }
    }

    fun tryLogin(email: String, password: String): Option<UserTableDTO> {
        return UserTable.select { UserTable.email eq email and (UserTable.password eq password) }.singleOrNone()
            .flatMap { UserTable.toDomain(it) }
    }
}
