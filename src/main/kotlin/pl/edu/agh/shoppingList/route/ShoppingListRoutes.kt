package pl.edu.agh.shoppingList.route

import arrow.core.right
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.auth.service.getLoggedUser
import pl.edu.agh.shoppingList.domain.ShoppingList
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.service.ShoppingListService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.getOption
import pl.edu.agh.utils.Utils.responsePair

object ShoppingListRoutes {
    private val logger by LoggerDelegate()

    fun Application.configureShoppingListRoutes() {
        val shoppingListService by inject<ShoppingListService>()

        routing() {
            authenticate(Roles.USER) {
                route("/shopping-lists") {
                    get("/my") {
                        Utils.handleOutput(call) {
                            getLoggedUser(call) { _, _, userId ->
                                shoppingListService.getAllShoppingListsByUserId(userId)
                                    .right()
                                    .responsePair(ShoppingList.serializer())
                            }
                        }
                    }
                    get("/{id}") {
                        val id = call.parameters.getOption("id").map { ShoppingListId(it.toInt()) }

                        Utils.handleOutput(call) {
                            id.fold(
                                ifEmpty = { Pair(HttpStatusCode.BadRequest, "Invalid id") },
                                ifSome = { id ->
                                    shoppingListService.getShoppingList(id)
                                        .toEither()
                                        .mapLeft {
                                            logger.warn(it.message)
                                            Pair(HttpStatusCode.BadRequest, "Could not get shopping list $id")
                                        }
                                        .responsePair(ShoppingList.serializer())
                                }
                            )
                        }
                    }
                    post("/") {
                        val shoppingListName = call.receive<String>()

                        Utils.handleOutput(call) {
                            getLoggedUser(call) { _, _, userId ->
                                shoppingListService
                                    .createShoppingList(userId, shoppingListName)
                                    .toEither()
                                    .mapLeft {
                                        logger.warn(it.message)
                                        Pair(
                                            HttpStatusCode.BadRequest,
                                            "Could not create shopping list for user: $userId with name: $shoppingListName"
                                        )
                                    }
                                    .responsePair(ShoppingList.serializer())
                            }
                        }
                    }
                    put("/{id}") {
                        val newShoppingListName = call.receive<String>()
                        val shoppingListId = call.parameters.getOption("id").map { ShoppingListId(it.toInt()) }

                        Utils.handleOutput(call) {
                            shoppingListId.fold(
                                ifEmpty = { Pair(HttpStatusCode.BadRequest, "Invalid id") },
                                ifSome = { shoppingListId ->
                                    getLoggedUser(call) { _, _, userId ->
                                        shoppingListService.updateShoppingList(
                                            userId,
                                            shoppingListId,
                                            newShoppingListName
                                        )
                                            .toEither()
                                            .mapLeft {
                                                logger.warn(it.message)
                                                Pair(
                                                    HttpStatusCode.BadRequest,
                                                    "List not found"
                                                )
                                            }
                                            .responsePair(ShoppingList.serializer())
                                    }
                                }
                            )
                        }
                    }
                    delete("/{id}") {
                        val shoppingListId = call.parameters.getOption("id").map { ShoppingListId(it.toInt()) }

                        Utils.handleOutput(call) {
                            shoppingListId.fold(
                                ifEmpty = { Pair(HttpStatusCode.BadRequest, "Invalid id") },
                                ifSome = { shoppingListId ->
                                    getLoggedUser(call) { _, _, userId ->
                                        shoppingListService.deleteShoppingList(userId, shoppingListId).toEither()
                                            .mapLeft {
                                                logger.warn(it.message)
                                                Pair(
                                                    HttpStatusCode.BadRequest,
                                                    "Could not delete shopping list $shoppingListId"

                                                )
                                            }
                                            .responsePair(ShoppingList.serializer())
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }


}
