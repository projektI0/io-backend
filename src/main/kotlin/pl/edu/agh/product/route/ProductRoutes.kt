package pl.edu.agh.product.route

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
import pl.edu.agh.product.domain.dto.ProductTableDTO
import pl.edu.agh.product.domain.request.ProductFilterRequest
import pl.edu.agh.product.domain.request.ProductRequest
import pl.edu.agh.product.service.ProductService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.getParam
import pl.edu.agh.utils.Utils.handleOutput
import pl.edu.agh.utils.Utils.responsePair
import pl.edu.agh.utils.Utils.toResponsePairLogging

object ProductRoutes {
    private const val DEFAULT_LIMIT_VALUE: Int = 100
    private const val DEFAULT_OFFSET_VALUE: Long = 0
    private val logger by LoggerDelegate()

    fun Application.configureProductRoutes() {
        val productService by inject<ProductService>()

        routing {
            authenticate(Roles.USER) {
                route("/products/filter") {
                    post {
                        logger.info("getting filtered products")
                        handleOutput(call) {
                            either {
                                val productFilterRequest = Utils.getBody<ProductFilterRequest>(call).bind()
                                val (_, _, userId) = getLoggedUser(call)

                                productService.getFilteredProducts(productFilterRequest, userId).toResponsePairLogging()
                                    .bind()
                            }.responsePair()
                        }
                    }
                }
                route("/products") {
                    get("/{id}") {
                        logger.info("getting product")
                        handleOutput(call) {
                            either {
                                val productId = getParam("id") { ProductId(it) }.bind()
                                val (_, _, userId) = getLoggedUser(call)

                                productService.getProduct(productId, userId).toResponsePairLogging().bind()
                            }.responsePair(ProductTableDTO.serializer())
                        }
                    }
                    get("") {
                        logger.info("getting products")
                        handleOutput(call) {
                            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT_VALUE
                            val offset = call.parameters["offset"]?.toLongOrNull() ?: DEFAULT_OFFSET_VALUE
                            val (_, _, userId) = getLoggedUser(call)

                            either<Pair<HttpStatusCode, String>, List<ProductTableDTO>> {
                                productService.getAllProducts(limit, offset, userId).right().bind()
                            }.responsePair(ProductTableDTO.serializer())
                        }
                    }
                    post("") {
                        logger.info("adding product")
                        handleOutput(call) {
                            either {
                                val productRequest = Utils.getBody<ProductRequest>(call).bind()
                                val (_, _, userId) = getLoggedUser(call)

                                productService
                                    .createProduct(productRequest, userId)
                                    .toResponsePairLogging()
                                    .bind()
                            }.responsePair(ProductTableDTO.serializer())
                        }
                    }
                }
            }
        }
    }
}
