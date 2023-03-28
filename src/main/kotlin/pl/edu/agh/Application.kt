package pl.edu.agh

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import org.koin.ktor.plugin.Koin
import pl.edu.agh.auth.AuthModule.getKoinAuthModule
import pl.edu.agh.auth.route.AuthRoutes.configureAuthRoutes
import pl.edu.agh.auth.service.configureSecurity
import pl.edu.agh.shop.route.ShopRoutes.configureShopRoutes
import pl.edu.agh.shop.ShopModule.getKoinShopModule
import pl.edu.agh.shoppingList.ShoppingListModule.getKoinShoppingListModule
import pl.edu.agh.shoppingList.route.ShoppingListRoutes.configureShoppingListRoutes
import pl.edu.agh.utils.*
import pl.edu.agh.utils.DatabaseConnector.initDB

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowHeadersPrefixed("")
        allowNonSimpleContentTypes = true
        anyHost()
    }
    initDB()
    install(Koin) {
        modules(getKoinAuthModule(), getKoinShoppingListModule(), getKoinShopModule())
    }
    configureSecurity()
    configureAuthRoutes()
    configureShoppingListRoutes()
    configureShopRoutes()
}
