package pl.edu.agh.auth.domain

import arrow.core.Option
import arrow.core.some
import kotlinx.serialization.Serializable

@Serializable
sealed class Roles(val id: Int, val roleName: String) {
    @Serializable
    object User : Roles(1, "USER")
    @Serializable
    object Admin : Roles(2, "ADMIN")
    companion object {
        fun fromId(id: Int?): Option<Roles> =
            when (id) {
                1 -> User.some()
                2 -> Admin.some()
                else -> throw IllegalArgumentException("Unknown role id: $id")
            }

        fun fromString(value: String?) =
            when (value) {
                "USER" -> User.some()
                "ADMIN" -> Admin.some()
                else -> throw IllegalArgumentException("Unknown role name: $value")
            }
    }
}
