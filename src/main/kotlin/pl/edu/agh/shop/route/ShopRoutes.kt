package pl.edu.agh.shop.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.auth.service.getLoggedUser
import pl.edu.agh.shop.domain.ShopData
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.domain.ShopsBoundsRequest
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
                            }.responsePair(ShopTableDTO.serializer())
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
                                val shopData = Utils.getBody<ShopData>(call).bind()
                                val (_, _, userId) = getLoggedUser(call)

                                shopService
                                    .createShop(shopData, userId)
                                    .toResponsePairLogging()
                                    .bind()
                            }.responsePair(ShopTableDTO.serializer())
                        }
                    }
                }
            }
        }
    }
}