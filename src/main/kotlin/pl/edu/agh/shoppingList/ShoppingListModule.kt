package pl.edu.agh.shoppingList

import io.ktor.server.application.*
import org.koin.dsl.module
import pl.edu.agh.shoppingList.service.ShoppingListService
import pl.edu.agh.shoppingList.service.ShoppingListServiceImpl

object ShoppingListModule {
    fun Application.getKoinShoppingListModule() =
        module {
            single<ShoppingListService> { ShoppingListServiceImpl() }
        }
}