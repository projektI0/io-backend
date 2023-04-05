package pl.edu.agh.shop

import io.ktor.server.application.*
import org.koin.dsl.module
import pl.edu.agh.shop.service.ShopService
import pl.edu.agh.shop.service.ShopServiceImpl

object ShopModule {
    fun Application.getKoinShopModule() =
        module {
            single<ShopService> { ShopServiceImpl() }
        }
}
