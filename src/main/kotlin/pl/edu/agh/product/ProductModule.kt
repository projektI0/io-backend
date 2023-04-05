package pl.edu.agh.product

import io.ktor.server.application.Application
import org.koin.dsl.module
import pl.edu.agh.product.service.ProductService
import pl.edu.agh.product.service.ProductServiceImpl

object ProductModule {

    fun Application.getKoinProductModule() = module {
        single<ProductService> { ProductServiceImpl() }
    }
}
