package pl.edu.agh.recipe.route

import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.service.authenticate
import pl.edu.agh.auth.service.getLoggedUser
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.request.ProductFilterRequest
import pl.edu.agh.recipe.domain.RecipeDTO
import pl.edu.agh.recipe.domain.RecipeRequest
import pl.edu.agh.recipe.service.RecipeService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.getParam
import pl.edu.agh.utils.Utils.responsePair
import pl.edu.agh.utils.Utils.toResponsePairLogging

object RecipeRoutes {
    private val logger by LoggerDelegate()
    private const val DEFAULT_LIMIT_VALUE: Int = 10
    private const val DEFAULT_OFFSET_VALUE: Long = 0

    fun Application.configureRecipeRoutes() {
        val recipeService by inject<RecipeService>()

        routing {
            authenticate(Roles.USER) {
                route("/recipes") {
                    get("/{id}") {
                        logger.info("getting recipe")
                        Utils.handleOutput(call) {
                            either {
                                val productId = getParam("id") { ProductId(it) }.bind()
                                val (_, _, userId) = getLoggedUser(call)

                                recipeService.getRecipe(productId, userId).toResponsePairLogging().bind()
                            }.responsePair(RecipeDTO.serializer())
                        }
                    }
                    get("") {
                        logger.info("getting recipes")
                        Utils.handleOutput(call) {
                            val (_, _, userId) = getLoggedUser(call)

                            either<Pair<HttpStatusCode, String>, List<RecipeDTO>> {
                                recipeService.getAllRecipes(userId).right().bind()
                            }.responsePair(RecipeDTO.serializer())
                        }
                    }
                    post("") {
                        logger.info("adding recipe")
                        Utils.handleOutput(call) {
                            either {
                                val recipeRequest = Utils.getBody<RecipeRequest>(call).bind()
                                val (_, _, userId) = getLoggedUser(call)

                                recipeService
                                    .createRecipe(recipeRequest, userId)
                                    .toResponsePairLogging()
                                    .bind()
                            }.responsePair(RecipeDTO.serializer())
                        }
                    }
                    route("/filter") {
                        post {
                            logger.info("getting filtered recipes")
                            Utils.handleOutput(call) {
                                either {
                                    val request = Utils.getBody<ProductFilterRequest>(call).bind()
                                    val limit =
                                        call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT_VALUE
                                    val offset =
                                        call.parameters["offset"]?.toLongOrNull() ?: DEFAULT_OFFSET_VALUE
                                    val (_, _, userId) = getLoggedUser(call)

                                    recipeService.getFilteredRecipes(request, limit, offset, userId)
                                        .toResponsePairLogging()
                                        .bind()
                                }.responsePair(RecipeDTO.serializer())
                            }
                        }
                    }
                }
            }
        }
    }
}
