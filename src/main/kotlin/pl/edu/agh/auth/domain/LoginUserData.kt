package pl.edu.agh.auth.domain

import kotlinx.serialization.Serializable

@Serializable
data class LoginUserData(val loginUserDTO: LoginUserDTO, val roles: List<Roles>, val jwtToken: String)
