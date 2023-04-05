package pl.edu.agh.shoppingList.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.auth.service.getLoggedUser
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.shoppingList.domain.ShoppingList
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListProduct
import pl.edu.agh.shoppingList.domain.ShoppingListView
import pl.edu.agh.shoppingList.service.ShoppingListService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils.getBody
import pl.edu.agh.utils.Utils.getParam
import pl.edu.agh.utils.Utils.handleOutput
import pl.edu.agh.utils.Utils.responsePair
import pl.edu.agh.utils.Utils.toResponsePairLogging

object ShoppingListRoutes {
    private val logger by LoggerDelegate()

    fun Application.configureShoppingListRoutes() {
        val shoppingListService by inject<ShoppingListService>()

        routing {
            authenticate(Roles.USER) {
                route("/shopping-lists") {
                    get("/my") {
                        handleOutput(call) {
                            either<Pair<HttpStatusCode, String>, List<ShoppingList>> {
                                val (_, _, loginUserId) = getLoggedUser(call)
                                shoppingListService.getAllShoppingListsByUserId(loginUserId).right().bind()
                            }.responsePair(ShoppingList.serializer())
                        }
                    }
                    get("/{id}") {
                        handleOutput(call) {
                            either {
                                val id = getParam("id") { ShoppingListId(it) }.bind()
                                val (_, _, loginUserId) = getLoggedUser(call)

                                shoppingListService.getShoppingList(loginUserId, id).toResponsePairLogging().bind()
                            }.responsePair(ShoppingList.serializer())
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
                            }.responsePair(ShoppingList.serializer())
                        }
                    }
                    put("/{id}") {
                        handleOutput(call) {
                            either {
                                val newShoppingListName = getBody<String>(call).bind()
                                val shoppingListId = getParam("id") { ShoppingListId(it) }.bind()
                                val (_, _, loginUserId) = getLoggedUser(call)

                                shoppingListService.updateShoppingList(
                                    loginUserId, shoppingListId, newShoppingListName
                                ).toResponsePairLogging().bind()
                            }.responsePair(ShoppingList.serializer())
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
                            }.responsePair(ShoppingList.serializer())
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
                                    val shoppingListProduct = getBody<ShoppingListProduct>(call).bind()
                                    val (_, _, loginUserId) = getLoggedUser(call)

                                    shoppingListService.addProductToList(
                                        loginUserId, shoppingListProduct
                                    ).toResponsePairLogging().bind()
                                }.responsePair()
                            }
                        }
                        put("/{id}") {
                            handleOutput(call) {
                                either {
                                    val shoppingListId = getParam("listId") { ShoppingListId(it) }.bind()
                                    val shoppingListProductId = getParam("id") { ProductId(it) }.bind()
                                    val shoppingListProduct = getBody<ShoppingListProduct>(call).bind()
                                    val (_, _, loginUserId) = getLoggedUser(call)

                                    shoppingListService.updateProductQuantity(
                                        loginUserId, shoppingListProduct
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
                                        userId, shoppingListId, productId
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
