package pl.edu.agh.shoppingList.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shoppingList.domain.ShoppingList
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListInput
import pl.edu.agh.shoppingList.table.ShoppingListTable
import pl.edu.agh.utils.LoggerDelegate

object ShoppingListDao {
    private val logger by LoggerDelegate()
    fun createShoppingList(shoppingListInput: ShoppingListInput): Option<ShoppingList> {
        logger.info("Creating shopping list $shoppingListInput")
        val returningId = ShoppingListTable.insert {
            it[name] = shoppingListInput.name
            it[ownerId] = shoppingListInput.userId
        }[ShoppingListTable.id]

        logger.info("Shopping list has been created with id $returningId ($shoppingListInput)")

        return unsafeGetShoppingList(returningId)
    }

    fun secureGetShoppingList(ownerId: LoginUserId, shoppingListId: ShoppingListId): Option<ShoppingList> =
        ShoppingListTable
            .select { (ShoppingListTable.id eq shoppingListId) and (ShoppingListTable.ownerId eq ownerId) }
            .firstOrNone()
            .map { ShoppingListTable.toDomain(it) }

    private fun unsafeGetShoppingList(id: ShoppingListId): Option<ShoppingList> =
        ShoppingListTable
            .select(ShoppingListTable.id eq id)
            .firstOrNone()
            .map { ShoppingListTable.toDomain(it) }


    fun updateShoppingList(id: ShoppingListId, shoppingListInput: ShoppingListInput): Option<ShoppingList> {
        ShoppingListTable.update({ ShoppingListTable.id eq id }) {
            it[name] = shoppingListInput.name
            it[ownerId] = shoppingListInput.userId
        }

        return unsafeGetShoppingList(id)
    }

    fun deleteShoppingList(id: ShoppingListId): Option<ShoppingList> {
        val shoppingList = unsafeGetShoppingList(id)
        ShoppingListTable
            .deleteWhere { ShoppingListTable.id eq id }
        return shoppingList
    }

    fun getAllShoppingListsByUserId(loginUserId: LoginUserId): List<ShoppingList> {
        return ShoppingListTable
            .select { ShoppingListTable.ownerId eq loginUserId }
            .map { ShoppingListTable.toDomain(it) }
    }
}

