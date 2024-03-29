package pl.edu.agh

import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.koin.ktor.plugin.Koin
import pl.edu.agh.auth.AuthModule.getKoinAuthModule
import pl.edu.agh.auth.route.AuthRoutes.configureAuthRoutes
import pl.edu.agh.auth.service.configureSecurity
import pl.edu.agh.path.PathModule.getKoinPathModule
import pl.edu.agh.path.route.PathRoutes.configurePathRoutes
import pl.edu.agh.product.ProductModule.getKoinProductModule
import pl.edu.agh.product.domain.dto.ProductTableDTO
import pl.edu.agh.product.route.ProductRoutes.configureProductRoutes
import pl.edu.agh.recipe.RecipeModule.getKoinRecipeModule
import pl.edu.agh.recipe.route.RecipeRoutes.configureRecipeRoutes
import pl.edu.agh.shop.ShopModule.getKoinShopModule
import pl.edu.agh.shop.route.ShopRoutes.configureShopRoutes
import pl.edu.agh.shoppingList.ShoppingListModule.getKoinShoppingListModule
import pl.edu.agh.shoppingList.route.ShoppingListRoutes.configureShoppingListRoutes
import pl.edu.agh.tag.TagModule.getKoinTagModule
import pl.edu.agh.tag.route.TagRoutes.configureTagRoutes
import pl.edu.agh.utils.DBQueryResponseWithCount
import pl.edu.agh.utils.DatabaseConnector.initDB

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                serializersModule =
                    SerializersModule { contextual(DBQueryResponseWithCount.serializer(ProductTableDTO.serializer())) }
            }
        )
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
        modules(
            getKoinAuthModule(),
            getKoinShoppingListModule(),
            getKoinProductModule(),
            getKoinShopModule(),
            getKoinPathModule(),
            getKoinTagModule(),
            getKoinRecipeModule()
        )
    }
    configureSecurity()
    configureAuthRoutes()
    configureShoppingListRoutes()
    configureProductRoutes()
    configureTagRoutes()
    configureShopRoutes()
    configurePathRoutes()
    configureRecipeRoutes()
}
