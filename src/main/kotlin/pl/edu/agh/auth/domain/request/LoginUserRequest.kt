package pl.edu.agh.auth.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginUserRequest(val email: String, val password: String)
