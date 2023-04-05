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
import pl.edu.agh.shoppingList.domain.ShoppingList
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListInput
import pl.edu.agh.shoppingList.domain.ShoppingListProduct
import pl.edu.agh.shoppingList.domain.ShoppingListView
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
    fun createShoppingList(userId: LoginUserId, name: String): Effect<ShoppingListCreationError, ShoppingList>
    fun getShoppingList(userId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingList>
    fun deleteShoppingList(userId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingList>
    suspend fun getAllShoppingListsByUserId(userId: LoginUserId): List<ShoppingList>
    fun updateShoppingList(
        userId: LoginUserId,
        id: ShoppingListId,
        name: String
    ): Effect<ListNotFoundError, ShoppingList>

    fun addProductToList(
        loginUserId: LoginUserId,
        shoppingListProduct: ShoppingListProduct
    ): Effect<ListNotFoundError, Unit>

    fun removeProductFromList(
        userId: LoginUserId,
        shoppingListId: ShoppingListId,
        productId: ProductId
    ): Effect<ListNotFoundError, Unit>

    fun updateProductQuantity(
        userId: LoginUserId,
        shoppingListProduct: ShoppingListProduct
    ): Effect<ListNotFoundError, Unit>

    suspend fun getAllShoppingListProducts(id: ShoppingListId): List<ShoppingListProduct>
    fun getShoppingListView(loginUserId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingListView>
}

class ShoppingListServiceImpl : ShoppingListService {
    override fun createShoppingList(
        userId: LoginUserId,
        name: String
    ): Effect<ShoppingListCreationError, ShoppingList> = effect {
        val shoppingListInput = ShoppingListInput(userId, name)
        Transactor.dbQuery {
            ShoppingListDao
                .createShoppingList(shoppingListInput)
                .bind {
                    ShoppingListCreationError(userId, name)
                }
        }
    }

    // Needs to have transaction in scope
    private suspend fun secureGetShoppingList(
        userId: LoginUserId,
        id: ShoppingListId
    ): Either<ListNotFoundError, ShoppingList> = either {
        ShoppingListDao
            .secureGetShoppingList(userId, id)
            .bind {
                ListNotFoundError(userId, id)
            }
    }

    override fun getShoppingList(
        userId: LoginUserId,
        id: ShoppingListId
    ): Effect<ListNotFoundError, ShoppingList> =
        effect {
            Transactor.dbQuery {
                secureGetShoppingList(userId, id).bind()
            }
        }

    override fun deleteShoppingList(userId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingList> =
        effect {
            Transactor.dbQuery {
                ShoppingListDao
                    .deleteShoppingList(id)
                    .bind {
                        ListNotFoundError(userId, id)
                    }
            }
        }

    override suspend fun getAllShoppingListsByUserId(userId: LoginUserId): List<ShoppingList> =
        Transactor.dbQuery { ShoppingListDao.getAllShoppingListsByUserId(userId) }

    override fun updateShoppingList(
        userId: LoginUserId,
        id: ShoppingListId,
        name: String
    ): Effect<ListNotFoundError, ShoppingList> = effect {
        val shoppingListInput = ShoppingListInput(userId, name)
        Transactor.dbQuery {
            ShoppingListDao
                .updateShoppingList(id, shoppingListInput)
                .bind {
                    ListNotFoundError(userId, id)
                }
        }
    }

    override fun addProductToList(
        loginUserId: LoginUserId,
        shoppingListProduct: ShoppingListProduct
    ): Effect<ListNotFoundError, Unit> =
        effect {
            Transactor.dbQuery {
                secureGetShoppingList(loginUserId, shoppingListProduct.shoppingListId).bind()

                ShoppingListProductDao.addProductToShoppingList(shoppingListProduct)
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

    override fun updateProductQuantity(
        userId: LoginUserId,
        shoppingListProduct: ShoppingListProduct
    ): Effect<ListNotFoundError, Unit> = effect {
        Transactor.dbQuery {
            secureGetShoppingList(userId, shoppingListProduct.shoppingListId).bind()

            ShoppingListProductDao.updateProductInShoppingList(shoppingListProduct)
        }
    }

    override suspend fun getAllShoppingListProducts(id: ShoppingListId): List<ShoppingListProduct> =
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
