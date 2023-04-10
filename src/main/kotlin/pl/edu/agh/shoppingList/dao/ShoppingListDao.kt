package pl.edu.agh.shoppingList.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.dto.ShoppingListDTO
import pl.edu.agh.shoppingList.table.ShoppingListTable
import pl.edu.agh.utils.LoggerDelegate

object ShoppingListDao {
    private val logger by LoggerDelegate()

    fun createShoppingList(userId: LoginUserId, name: String): Option<ShoppingListDTO> {
        logger.info("Creating shopping list $name")
        val returningId = ShoppingListTable.insert {
            it[ShoppingListTable.name] = name
            it[ShoppingListTable.ownerId] = userId
        }[ShoppingListTable.id]

        logger.info("Shopping list has been created with id $returningId ($name)")

        return unsafeGetShoppingList(returningId)
    }

    fun secureGetShoppingList(ownerId: LoginUserId, shoppingListId: ShoppingListId): Option<ShoppingListDTO> =
        ShoppingListTable
            .select { (ShoppingListTable.id eq shoppingListId) and (ShoppingListTable.ownerId eq ownerId) }
            .firstOrNone()
            .map { ShoppingListTable.toDomain(it) }

    private fun unsafeGetShoppingList(id: ShoppingListId): Option<ShoppingListDTO> =
        ShoppingListTable
            .select(ShoppingListTable.id eq id)
            .firstOrNone()
            .map { ShoppingListTable.toDomain(it) }

    fun updateShoppingList(id: ShoppingListId, shoppingListName: String, userId: LoginUserId): Option<ShoppingListDTO> {
        ShoppingListTable.update({ ShoppingListTable.id eq id }) {
            it[name] = shoppingListName
            it[ownerId] = userId
        }

        return unsafeGetShoppingList(id)
    }

    fun deleteShoppingList(id: ShoppingListId): Option<ShoppingListDTO> {
        val shoppingList = unsafeGetShoppingList(id)
        ShoppingListTable
            .deleteWhere { ShoppingListTable.id eq id }
        return shoppingList
    }

    fun getAllShoppingListsByUserId(limit: Int, offset: Long, loginUserId: LoginUserId): List<ShoppingListDTO> {
        return ShoppingListTable
            .select { ShoppingListTable.ownerId eq loginUserId }
            .limit(limit, offset)
            .map { ShoppingListTable.toDomain(it) }
    }
}
