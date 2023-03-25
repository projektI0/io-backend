package pl.edu.agh.shoppingList.service

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shoppingList.dao.ShoppingListDao
import pl.edu.agh.shoppingList.domain.ShoppingList
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListInput
import pl.edu.agh.utils.Transactor

class ShoppingListCreationError(val message: String)
class ListNotFoundError(val message: String)

interface ShoppingListService {
    fun createShoppingList(userId: LoginUserId, name: String): Effect<ShoppingListCreationError, ShoppingList>
    fun getShoppingList(id: ShoppingListId): Effect<ListNotFoundError, ShoppingList>
    fun deleteShoppingList(userId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingList>
    suspend fun getAllShoppingListsByUserId(userId: LoginUserId): List<ShoppingList>
    fun updateShoppingList(
        userId: LoginUserId, id: ShoppingListId, name: String
    ): Effect<ListNotFoundError, ShoppingList>
}

class ShoppingListServiceImpl : ShoppingListService {
    override fun createShoppingList(
        userId: LoginUserId, name: String
    ): Effect<ShoppingListCreationError, ShoppingList> = effect {
        val shoppingListInput = ShoppingListInput(userId, name)
        Transactor.dbQuery {
            ShoppingListDao
                .createShoppingList(shoppingListInput)
                .bind {
                    ShoppingListCreationError("List creation error for $shoppingListInput")
                }
        }
    }

    override fun getShoppingList(id: ShoppingListId): Effect<ListNotFoundError, ShoppingList> =
        effect {
            Transactor.dbQuery {
                ShoppingListDao
                    .getShoppingList(id)
                    .bind {
                        ListNotFoundError("List not found for $id")
                    }
            }
        }

    override fun deleteShoppingList(userId: LoginUserId, id: ShoppingListId): Effect<ListNotFoundError, ShoppingList> =
        effect {
            Transactor.dbQuery {
                ShoppingListDao
                    .deleteShoppingList(id)
                    .bind {
                        ListNotFoundError("Could not delete shopping list $id for user: $userId")
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
                    ListNotFoundError("Could not update shopping list $id for user: $userId with name: $name")
                }
        }
    }
}