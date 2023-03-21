package pl.edu.agh.auth.service

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flattenOption
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.utils.getLogger

fun Application.configureSecurity() {
    val jwtConfig by inject<JWTConfig>()

    install(Authentication) {
        fun jwtPA(role: Roles) = run {
            jwt(
                role,
                jwtConfig
            )
        }

        jwtPA(Roles.User)
        jwtPA(Roles.Admin)
    }
}

fun Route.authenticate(
    vararg roles: Roles,
    build: Route.() -> Unit
): Route {
    return authenticate(*roles.map { it.roleName }.toTypedArray()) {
        build()
    }
}

fun Application.getJWTConfig(): JWTConfig {
    return JWTConfig(
        this.jwtAudience(),
        this.jwtRealm(),
        this.jwtSecret(),
        this.jwtDomain()
    )
}

private fun Application.getConfigProperty(path: String): String {
    return this.environment.config
        .property(path)
        .getString()
}


data class JWTConfig(
    val audience: String,
    val realm: String,
    val secret: String,
    val domain: String
)

private fun Application.jwtAudience(): String {
    return this.getConfigProperty("jwt.audience")
}

private fun Application.jwtRealm(): String {
    return this.getConfigProperty("jwt.realm")
}

private fun Application.jwtSecret(): String {
    return this.getConfigProperty("jwt.secret")
}

private fun Application.jwtDomain(): String {
    return this.getConfigProperty("jwt.domain")
}

private fun JWTCredential.validateRole(role: Roles): Either<String, JWTCredential> =
    Either.conditionally(payload.getClaim("roles").asList(String::class.java).map { Roles.fromString(it) }
        .flattenOption().contains(role), { "Invalid role" }, { this })


fun AuthenticationConfig.jwt(
    name: Roles, jwtConfig: JWTConfig
) {
    jwt(name.roleName) {
        verifier(
            JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                .withAudience(jwtConfig.audience)
                .withIssuer(jwtConfig.domain)
                .withClaimPresence("name")
                .withClaimPresence("roles")
                .acceptExpiresAt(Long.MAX_VALUE) // so everytime Expires at is ok
                .build()
        )
        validate { credential ->
            val credentialEither: Either<String, JWTCredential> = either {
                credential.validateRole(name).bind()
                credential
            }

            credentialEither.fold({
                getLogger(AuthenticationConfig::class.java).warn(
                    it
                )
                null
            }, {
                JWTPrincipal(
                    credential.payload
                )
            })
        }
        challenge { _, _ ->
            call.respond(
                io.ktor.http.HttpStatusCode.Unauthorized,
                mapOf("error" to "Unauthorized")
            )
        }
    }

}
