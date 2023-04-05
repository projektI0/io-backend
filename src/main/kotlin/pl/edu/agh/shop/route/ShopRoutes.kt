package pl.edu.agh.shop.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.domain.ShopsBoundsRequest
import pl.edu.agh.shop.service.ShopService
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.responsePair

object ShopRoutes {
    fun Application.configureShopRoutes() {
        val shopService by inject<ShopService>()

        routing {
            authenticate(Roles.USER) {
                route("/shops") {
                    get {
                        Utils.handleOutput(call) {
                            either<Pair<HttpStatusCode, String>, List<ShopTableDTO>> {
                                shopService.getAllShops().right().bind()
                            }.responsePair(ShopTableDTO.serializer())
                        }
                    }
                    post("/filter") {
                        Utils.handleOutput(call) {
                            either {
                                val shopsBoundsRequest = Utils.getBody<ShopsBoundsRequest>(call).bind()
                                shopService.getAllShopsWithinBounds(shopsBoundsRequest).right().bind()
                            }.responsePair(ShopTableDTO.serializer())
                        }
                    }
                }
            }
        }
    }
}
