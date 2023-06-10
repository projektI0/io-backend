package pl.edu.agh.recipe.service

import arrow.core.Either
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import io.ktor.http.HttpStatusCode
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.product.dao.ProductDao
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.request.ProductFilterRequest
import pl.edu.agh.product.service.PaginationError
import pl.edu.agh.recipe.domain.RecipeDTO
import pl.edu.agh.recipe.domain.RecipeRequest
import pl.edu.agh.utils.DomainException
import pl.edu.agh.utils.Transactor

class RecipeCreationError(name: String, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.BadRequest,
        "Could not create recipe $name for user $userId",
        "Could not create recipe $name for user $userId"
    )

class RecipeNotFound(productId: ProductId, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.NotFound,
        "Recipe $productId not found for user $userId",
        "Recipe $productId not found for user $userId"
    )

interface RecipeService {
    fun createRecipe(
        recipeRequest: RecipeRequest,
        userId: LoginUserId
    ): Effect<RecipeCreationError, RecipeDTO>

    suspend fun getAllRecipes(userId: LoginUserId): List<RecipeDTO>

    fun getRecipe(recipeId: ProductId, userId: LoginUserId): Effect<RecipeNotFound, RecipeDTO>

    fun getFilteredRecipes(
        request: ProductFilterRequest,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Effect<PaginationError, List<RecipeDTO>>
}

class RecipeServiceImpl : RecipeService {

    override suspend fun getAllRecipes(userId: LoginUserId): List<RecipeDTO> =
        Transactor.dbQuery {
            ProductDao.getAllRecipes(userId)
        }

    override fun getRecipe(recipeId: ProductId, userId: LoginUserId): Effect<RecipeNotFound, RecipeDTO> =
        effect {
            Transactor.dbQuery {
                ProductDao.getRecipe(recipeId, userId).bind {
                    RecipeNotFound(recipeId, userId)
                }
            }
        }

    override fun getFilteredRecipes(
        request: ProductFilterRequest,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Effect<PaginationError, List<RecipeDTO>> =
        effect {
            Either.conditionally(
                test = limit <= 0 || offset < 0,
                ifTrue = { PaginationError(limit, offset) },
                ifFalse = {
                    Transactor.dbQuery {
                        ProductDao.getFilteredRecipes(
                            request,
                            limit,
                            offset,
                            userId
                        )()
                    }
                }
            ).swap().bind()
        }

    override fun createRecipe(
        recipeRequest: RecipeRequest,
        userId: LoginUserId
    ): Effect<RecipeCreationError, RecipeDTO> = effect {
        Transactor.dbQuery {
            ProductDao
                .insertNewRecipe(recipeRequest, userId)
                .bind {
                    RecipeCreationError(recipeRequest.name, userId)
                }
        }
    }
}
