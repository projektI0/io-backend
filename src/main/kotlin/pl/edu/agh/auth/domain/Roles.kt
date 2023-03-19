package pl.edu.agh.auth.domain

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import kotlinx.serialization.Serializable

@Serializable
sealed class Roles(val id: Int, val roleName: String) {
    object User : Roles(1, "USER")
    object Admin : Roles(2, "ADMIN")
    companion object {
        fun fromString(it: String?): Option<Roles> {
            return when (it) {
                "USER" -> User.some()
                "ADMIN" -> Admin.some()
                else -> none()
            }
        }

        fun fromId(it: Int?): Option<Roles> {
            return when (it) {
                1 -> User.some()
                2 -> Admin.some()
                else -> throw IllegalArgumentException("Unknown role id: $it")
            }
        }
    }
}
