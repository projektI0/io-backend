package pl.edu.agh.shoppingList.service

import arrow.core.Either
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import arrow.core.continuations.either
import io.ktor.http.HttpStatusCode
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.shoppingList.dao.ShoppingListDao
import pl.edu.agh.shoppingList.dao.ShoppingListProductDao
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListProductView
import pl.edu.agh.shoppingList.domain.ShoppingListView
import pl.edu.agh.shoppingList.domain.dto.ShoppingListDTO
import pl.edu.agh.shoppingList.domain.dto.ShoppingListProductDTO
import pl.edu.agh.utils.DomainException
import pl.edu.agh.utils.Transactor

class ShoppingListCreationError(userId: LoginUserId, name: String) :
    DomainException(
        HttpStatusCode.BadRequest,
        "Could not create shopping list $name for user $userId",
        "Could not create shopping list $name"
    )

class ListNotFoundError(userId: LoginUserId, listId: ShoppingListId) :
    DomainException(HttpStatusCode.BadRequest, "List $listId not found for user $userId", "List $listId not found")

interface ShoppingListService {
    fun createShoppingList(
        userId: LoginUserId,
        name: String
    ): Effect<ShoppingListCreationError, ShoppingListDTO>

    fun getShoppingList(userId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingListDTO>
    fun deleteShoppingList(userId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingListDTO>
    suspend fun getAllShoppingListsByUserId(
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): List<ShoppingListDTO>

    fun updateShoppingList(
        userId: LoginUserId,
        id: ShoppingListId,
        name: String
    ): Effect<ListNotFoundError, ShoppingListDTO>

    fun addProductToList(
        loginUserId: LoginUserId,
        shoppingListProductDTO: ShoppingListProductDTO
    ): Effect<ListNotFoundError, ShoppingListProductView>

    fun removeProductFromList(
        userId: LoginUserId,
        shoppingListId: ShoppingListId,
        productId: ProductId
    ): Effect<ListNotFoundError, Unit>

    fun updateProductInList(
        userId: LoginUserId,
        shoppingListProductDTO: ShoppingListProductDTO
    ): Effect<ListNotFoundError, Unit>

    suspend fun getAllShoppingListProducts(id: ShoppingListId): List<ShoppingListProductDTO>
    fun getShoppingListView(loginUserId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingListView>
}

class ShoppingListServiceImpl : ShoppingListService {
    override fun createShoppingList(
        userId: LoginUserId,
        name: String
    ): Effect<ShoppingListCreationError, ShoppingListDTO> = effect {
        Transactor.dbQuery {
            ShoppingListDao
                .createShoppingList(userId, name)
                .bind {
                    ShoppingListCreationError(userId, name)
                }
        }
    }

    // Needs to have transaction in scope
    private suspend fun secureGetShoppingList(
        userId: LoginUserId,
        id: ShoppingListId
    ): Either<ListNotFoundError, ShoppingListDTO> = either {
        ShoppingListDao
            .secureGetShoppingList(userId, id)
            .bind {
                ListNotFoundError(userId, id)
            }
    }

    override fun getShoppingList(
        userId: LoginUserId,
        id: ShoppingListId
    ): Effect<ListNotFoundError, ShoppingListDTO> =
        effect {
            Transactor.dbQuery {
                secureGetShoppingList(userId, id).bind()
            }
        }

    override fun deleteShoppingList(
        userId: LoginUserId,
        id: ShoppingListId
    ): Effect<ListNotFoundError, ShoppingListDTO> =
        effect {
            Transactor.dbQuery {
                ShoppingListDao
                    .deleteShoppingList(id)
                    .bind {
                        ListNotFoundError(userId, id)
                    }
            }
        }

    override suspend fun getAllShoppingListsByUserId(
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): List<ShoppingListDTO> =
        Transactor.dbQuery { ShoppingListDao.getAllShoppingListsByUserId(limit, offset, userId) }

    override fun updateShoppingList(
        userId: LoginUserId,
        id: ShoppingListId,
        name: String
    ): Effect<ListNotFoundError, ShoppingListDTO> = effect {
        Transactor.dbQuery {
            ShoppingListDao
                .updateShoppingList(id, name, userId)
                .bind {
                    ListNotFoundError(userId, id)
                }
        }
    }

    override fun addProductToList(
        loginUserId: LoginUserId,
        shoppingListProductDTO: ShoppingListProductDTO
    ): Effect<ListNotFoundError, ShoppingListProductView> =
        effect {
            Transactor.dbQuery {
                secureGetShoppingList(loginUserId, shoppingListProductDTO.shoppingListId).bind()
                ShoppingListProductDao.addProductToShoppingList(shoppingListProductDTO)
                    .bind {
                        ListNotFoundError(loginUserId, shoppingListProductDTO.shoppingListId)
                    }
            }
        }

    override fun removeProductFromList(
        userId: LoginUserId,
        shoppingListId: ShoppingListId,
        productId: ProductId
    ): Effect<ListNotFoundError, Unit> = effect {
        Transactor.dbQuery {
            secureGetShoppingList(userId, shoppingListId).bind()

            ShoppingListProductDao.deleteProductFromShoppingList(shoppingListId, productId)
        }
    }

    override fun updateProductInList(
        userId: LoginUserId,
        shoppingListProductDTO: ShoppingListProductDTO
    ): Effect<ListNotFoundError, Unit> = effect {
        Transactor.dbQuery {
            secureGetShoppingList(userId, shoppingListProductDTO.shoppingListId).bind()

            ShoppingListProductDao.updateProductInShoppingList(shoppingListProductDTO)
        }
    }

    override suspend fun getAllShoppingListProducts(id: ShoppingListId): List<ShoppingListProductDTO> =
        Transactor.dbQuery {
            ShoppingListProductDao.getAllShoppingListProducts(id)
        }

    override fun getShoppingListView(
        loginUserId: LoginUserId,
        id: ShoppingListId
    ): Effect<ListNotFoundError, ShoppingListView> = effect {
        Transactor.dbQuery {
            val shoppingList = secureGetShoppingList(loginUserId, id)
                .bind()

            val productsView = ShoppingListProductDao.getAllShoppingListProductsView(id)

            ShoppingListView(shoppingList, productsView)
        }
    }
}
