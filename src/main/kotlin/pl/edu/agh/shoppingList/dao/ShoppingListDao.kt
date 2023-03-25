package pl.edu.agh.shoppingList.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shoppingList.domain.ShoppingList
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.ShoppingListInput
import pl.edu.agh.shoppingList.table.ShoppingListTable
import pl.edu.agh.utils.LoggerDelegate

object ShoppingListDao {
    val logger by LoggerDelegate()
    fun createShoppingList(shoppingListInput: ShoppingListInput): Option<ShoppingList> {
        logger.info("Creating shopping list $shoppingListInput")
        val returningId = ShoppingListTable.insert {
            it[ShoppingListTable.name] = shoppingListInput.name
            it[ShoppingListTable.ownerId] = shoppingListInput.userId
        }[ShoppingListTable.id]

        logger.info("Shopping list has been created with id $returningId ($shoppingListInput)")

        return getShoppingList(returningId)
    }

    fun getShoppingList(id: ShoppingListId): Option<ShoppingList> =
        ShoppingListTable
            .select(ShoppingListTable.id eq id)
            .firstOrNone()
            .map { ShoppingListTable.toDomain(it) }


    fun updateShoppingList(id: ShoppingListId, shoppingListInput: ShoppingListInput): Option<ShoppingList> {
        ShoppingListTable.update({ ShoppingListTable.id eq id }) {
            it[ShoppingListTable.name] = shoppingListInput.name
            it[ShoppingListTable.ownerId] = shoppingListInput.userId
        }

        return getShoppingList(id)
    }

    fun deleteShoppingList(id: ShoppingListId): Option<ShoppingList> {
        val shoppingList = getShoppingList(id)
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

