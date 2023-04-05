package pl.edu.agh.product.route

import arrow.core.continuations.either
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import pl.edu.agh.product.domain.ProductFilterRequest
import pl.edu.agh.product.service.ProductService
import pl.edu.agh.utils.LoggerDelegate
import pl.edu.agh.utils.Utils
import pl.edu.agh.utils.Utils.handleOutput
import pl.edu.agh.utils.Utils.responsePair
import pl.edu.agh.utils.Utils.toResponsePairLogging

object ProductRoutes {
    private val logger by LoggerDelegate()

    fun Application.configureProductRoutes() {
        logger.info("Configuring product routes")
        val productService by inject<ProductService>()

        routing {
            post("/products") {
                logger.info("getting products")
                handleOutput(call) {
                    either {
                        val productFilterRequest = Utils.getBody<ProductFilterRequest>(call).bind()

                        productService.getProducts(productFilterRequest).toResponsePairLogging().bind()
                    }.responsePair()
                }
            }
        }
    }
}
