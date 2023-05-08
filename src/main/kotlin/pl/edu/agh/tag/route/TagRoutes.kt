package pl.edu.agh.tag.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.tag.domain.dto.TagTableDTO
import pl.edu.agh.tag.service.TagService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.responsePair

object TagRoutes {
    private val logger by LoggerDelegate()

    fun Application.configureTagRoutes() {
        val tagService by inject<TagService>()

        routing {
            authenticate(Roles.USER) {
                route("/tags") {
                    get("") {
                        logger.info("getting tags")
                        Utils.handleOutput(call) {
                            either<Pair<HttpStatusCode, String>, List<TagTableDTO>> {
                                tagService.getAllTags().right().bind()
                            }.responsePair(TagTableDTO.serializer())
                        }
                    }
                }
            }
        }
    }
}
