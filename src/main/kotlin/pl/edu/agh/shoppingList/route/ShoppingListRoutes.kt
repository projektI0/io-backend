package pl.edu.agh.shoppingList.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.auth.service.getLoggedUser
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListView
import pl.edu.agh.shoppingList.domain.dto.ShoppingListDTO
import pl.edu.agh.shoppingList.domain.dto.ShoppingListProductDTO
import pl.edu.agh.shoppingList.service.ShoppingListService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils.getBody
import pl.edu.agh.utils.Utils.getParam
import pl.edu.agh.utils.Utils.handleOutput
import pl.edu.agh.utils.Utils.responsePair
import pl.edu.agh.utils.Utils.toResponsePairLogging

object ShoppingListRoutes {
    private const val DEFAULT_LIMIT_VALUE: Int = 100
    private const val DEFAULT_OFFSET_VALUE: Long = 0
    private val logger by LoggerDelegate()

    fun Application.configureShoppingListRoutes() {
        val shoppingListService by inject<ShoppingListService>()

        routing {
            authenticate(Roles.USER) {
                route("/shopping-lists") {
                    get("/my") {
                        handleOutput(call) {
                            either<Pair<HttpStatusCode, String>, List<ShoppingListDTO>> {
                                val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT_VALUE
                                val offset = call.parameters["offset"]?.toLongOrNull() ?: DEFAULT_OFFSET_VALUE
                                val (_, _, loginUserId) = getLoggedUser(call)
                                shoppingListService.getAllShoppingListsByUserId(limit, offset, loginUserId).right()
                                    .bind()
                            }.responsePair(ShoppingListDTO.serializer())
                        }
                    }
                    get("/{id}") {
                        handleOutput(call) {
                            either {
                                val id = getParam("id") { ShoppingListId(it) }.bind()
                                val (_, _, loginUserId) = getLoggedUser(call)

                                shoppingListService.getShoppingList(loginUserId, id).toResponsePairLogging().bind()
                            }.responsePair(ShoppingListDTO.serializer())
                        }
                    }
                    post("/") {
                        handleOutput(call) {
                            either {
                                val shoppingListName = getBody<String>(call).bind()
                                val (_, _, loginUserId) = getLoggedUser(call)

                                shoppingListService
                                    .createShoppingList(loginUserId, shoppingListName)
                                    .toResponsePairLogging()
                                    .bind()
                            }.responsePair(ShoppingListDTO.serializer())
                        }
                    }
                    put("/{id}") {
                        handleOutput(call) {
                            either {
                                val newShoppingListName = getBody<String>(call).bind()
                                val shoppingListId = getParam("id") { ShoppingListId(it) }.bind()
                                val (_, _, loginUserId) = getLoggedUser(call)

                                shoppingListService.updateShoppingList(
                                    loginUserId,
                                    shoppingListId,
                                    newShoppingListName
                                ).toResponsePairLogging().bind()
                            }.responsePair(ShoppingListDTO.serializer())
                        }
                    }
                    delete("/{id}") {
                        handleOutput(call) {
                            either {
                                val shoppingListId = getParam("id") { ShoppingListId(it) }.bind()
                                val (_, _, loginUserId) = getLoggedUser(call)

                                shoppingListService.deleteShoppingList(loginUserId, shoppingListId)
                                    .toResponsePairLogging()
                                    .bind()
                            }.responsePair(ShoppingListDTO.serializer())
                        }
                    }
                    get("/{id}/view") {
                        handleOutput(call) {
                            either {
                                val shoppingListId = getParam("id") { ShoppingListId(it) }.bind()
                                val (_, _, loginUserId) = getLoggedUser(call)

                                shoppingListService.getShoppingListView(loginUserId, shoppingListId)
                                    .toResponsePairLogging()
                                    .bind()
                            }.responsePair(ShoppingListView.serializer())
                        }
                    }
                    route("/{listId}/products") {
                        post("/") {
                            handleOutput(call) {
                                either {
                                    val shoppingListId = getParam("listId") { ShoppingListId(it) }
                                    val shoppingListProductDTO = getBody<ShoppingListProductDTO>(call).bind()
                                    val (_, _, loginUserId) = getLoggedUser(call)

                                    shoppingListService.addProductToList(
                                        loginUserId,
                                        shoppingListProductDTO
                                    ).toResponsePairLogging().bind()
                                }.responsePair()
                            }
                        }
                        put("/{id}") {
                            handleOutput(call) {
                                either {
                                    val shoppingListId = getParam("listId") { ShoppingListId(it) }.bind()
                                    val shoppingListProductId = getParam("id") { ProductId(it) }.bind()
                                    val shoppingListProductDTO = getBody<ShoppingListProductDTO>(call).bind()
                                    val (_, _, loginUserId) = getLoggedUser(call)

                                    shoppingListService.updateProductQuantity(
                                        loginUserId,
                                        shoppingListProductDTO
                                    ).toResponsePairLogging().bind()
                                }.responsePair()
                            }
                        }
                        delete("/{id}") {
                            handleOutput(call) {
                                either {
                                    val shoppingListId =
                                        getParam("listId") { value: Int -> ShoppingListId(value) }.bind()
                                    val productId = getParam("id") { value: Int -> ProductId(value) }.bind()
                                    val (_, _, userId) = getLoggedUser(call)

                                    shoppingListService.removeProductFromList(
                                        userId,
                                        shoppingListId,
                                        productId
                                    ).toResponsePairLogging().bind()
                                }.responsePair()
                            }
                        }
                    }
                }
            }
        }
    }
}
