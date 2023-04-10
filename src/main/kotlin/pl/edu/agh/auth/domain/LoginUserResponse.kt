package pl.edu.agh.auth.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.auth.domain.dto.LoginUserDTO

/**
 * Response to user register/login action
 */

@Serializable
data class LoginUserResponse(val loginUserDTO: LoginUserDTO, val roles: List<Roles>, val jwtToken: String)
