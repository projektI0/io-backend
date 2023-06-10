package pl.edu.agh.recipe

import io.ktor.server.application.Application
import org.koin.dsl.module
import pl.edu.agh.recipe.service.RecipeService
import pl.edu.agh.recipe.service.RecipeServiceImpl

object RecipeModule {
    fun Application.getKoinRecipeModule() = module {
        single<RecipeService> { RecipeServiceImpl() }
    }
}
