package pl.edu.agh.path.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.auth.service.getLoggedUser
import pl.edu.agh.path.domain.PathRequest
import pl.edu.agh.path.domain.PathResponse
import pl.edu.agh.path.service.PathService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Transactor
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.responsePair

object PathRoutes {
    private val logger by LoggerDelegate()

    fun Application.configurePathRoutes() {
        val pathService by inject<PathService>()

        routing {
            authenticate(Roles.USER) {
                route("/path") {
                    post("") {
                        logger.info("creating path")
                        Utils.handleOutput(call) {
                            either {
                                val pathRequest = Utils.getBody<PathRequest>(call).bind()
                                val (_, _, userId) = getLoggedUser(call)

                                Transactor.dbQuery {
                                    pathService.findOptimalRoute(pathRequest, userId)
                                }.right().bind()
                            }.responsePair(PathResponse.serializer())
                        }
                    }
                }
            }
        }
    }
}
