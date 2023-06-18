package pl.edu.agh.shop.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.auth.service.getLoggedUser
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.dto.ShopMapDTO
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.shop.domain.request.ShopRequest
import pl.edu.agh.shop.domain.request.ShopsBoundsRequest
import pl.edu.agh.shop.service.ShopService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.getParam
import pl.edu.agh.utils.Utils.responsePair
import pl.edu.agh.utils.Utils.toResponsePairLogging

object ShopRoutes {
    private const val DEFAULT_LIMIT_VALUE: Int = 100
    private const val DEFAULT_OFFSET_VALUE: Long = 0
    private val logger by LoggerDelegate()

    fun Application.configureShopRoutes() {
        val shopService by inject<ShopService>()

        routing {
            authenticate(Roles.USER) {
                route("/shops") {
                    post("/filter") {
                        logger.info("getting filtered bounds shops")
                        Utils.handleOutput(call) {
                            either {
                                val shopsBoundsRequest = Utils.getBody<ShopsBoundsRequest>(call).bind()
                                val (_, _, userId) = getLoggedUser(call)

                                shopService.getAllShopsWithinBounds(shopsBoundsRequest, userId).right().bind()
                            }.responsePair(ShopMapDTO.serializer())
                        }
                    }
                    get("/{id}") {
                        logger.info("getting shop")
                        Utils.handleOutput(call) {
                            either {
                                val shopId = getParam("id") { ShopId(it) }.bind()
                                val (_, _, userId) = getLoggedUser(call)

                                shopService.getShop(shopId, userId).toResponsePairLogging().bind()
                            }.responsePair(ShopTableDTO.serializer())
                        }
                    }
                    get("") {
                        logger.info("getting shops")
                        Utils.handleOutput(call) {
                            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT_VALUE
                            val offset = call.parameters["offset"]?.toLongOrNull() ?: DEFAULT_OFFSET_VALUE
                            val (_, _, userId) = getLoggedUser(call)

                            either<Pair<HttpStatusCode, String>, List<ShopTableDTO>> {
                                shopService.getAllShops(limit, offset, userId).right().bind()
                            }.responsePair(ShopTableDTO.serializer())
                        }
                    }
                    post("") {
                        logger.info("adding shop")
                        Utils.handleOutput(call) {
                            either {
                                val shopRequest = Utils.getBody<ShopRequest>(call).bind()
                                val (_, _, userId) = getLoggedUser(call)

                                shopService
                                    .createShop(shopRequest, userId)
                                    .toResponsePairLogging()
                                    .bind()
                            }.responsePair(ShopTableDTO.serializer())
                        }
                    }
                    post("blacklist/{id}") {
                        logger.info("adding shop to blacklist")
                        Utils.handleOutput(call) {
                            either {
                                val shopId = getParam("id") { ShopId(it) }.bind()
                                val (_, _, userId) = getLoggedUser(call)

                                shopService
                                    .addShopToUserBlacklist(shopId, userId).toResponsePairLogging().bind()
                            }.responsePair()
                        }
                    }
                    delete("blacklist/{id}") {
                        logger.info("deleting shop from blacklist")
                        Utils.handleOutput(call) {
                            either {
                                val shopId = getParam("id") { ShopId(it) }.bind()
                                val (_, _, userId) = getLoggedUser(call)

                                shopService
                                    .removeShopFromUserBlacklist(shopId, userId).toResponsePairLogging().bind()
                            }.responsePair()
                        }
                    }
                }
            }
        }
    }
}
