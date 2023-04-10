package pl.edu.agh.auth.domain.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.auth.domain.LoginUserId

@Serializable
data class LoginUserDTO(val id: LoginUserId, val email: String)
