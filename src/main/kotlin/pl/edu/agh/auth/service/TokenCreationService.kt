package pl.edu.agh.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import pl.edu.agh.auth.domain.Roles

typealias JWTTokenSimple = String

class TokenCreationService(private val jwtConfig: JWTConfig) {
    fun createToken(name: String, roles: List<Roles>): JWTTokenSimple {
        return JWT
            .create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.domain)
            .withClaim("name", name)
            .withClaim("roles", roles.map { it.roleName })
            .sign(Algorithm.HMAC256(jwtConfig.secret))
    }
}
