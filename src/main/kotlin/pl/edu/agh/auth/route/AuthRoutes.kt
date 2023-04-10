package pl.edu.agh.auth.route

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.LoginUserResponse
import pl.edu.agh.auth.domain.request.LoginUserRequest
import pl.edu.agh.auth.service.AuthService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Transactor
import pl.edu.agh.utils.Utils.handleOutput
import pl.edu.agh.utils.Utils.responsePair

object AuthRoutes {
    private val logger by LoggerDelegate()

    fun Application.configureAuthRoutes() {
        val authService by inject<AuthService>()

        routing {
            post("/register") {
                handleOutput(call) {
                    Transactor.dbQuery {
                        val userData = call.receive<LoginUserRequest>()
                        val signedUserResponse = authService.signUpNewUser(userData)
                        signedUserResponse.mapLeft {
                            it.toResponsePairLogging()
                        }.responsePair(LoginUserResponse.serializer())
                    }
                }
            }

            post("/login") {
                handleOutput(call) {
                    Transactor.dbQuery {
                        val userData = call.receive<LoginUserRequest>()
                        val signedUserResponse = authService.signInUser(userData)
                        signedUserResponse.mapLeft {
                            it.toResponsePairLogging()
                        }.responsePair(LoginUserResponse.serializer())
                    }
                }
            }
        }
    }
}
