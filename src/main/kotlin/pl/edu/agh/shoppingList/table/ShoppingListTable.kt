package pl.edu.agh.shoppingList.table

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.loginUserId
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.shoppingListId
import java.time.Instant

object ShoppingListTable : IdTable<ShoppingListId>("SHOPPING_LIST") {
    override val id = shoppingListId("ID").autoIncrement().entityId()
    val name: Column<String?> = varchar("NAME", 256).nullable()
    val ownerId: Column<LoginUserId> = loginUserId("OWNER_ID")
    val createdAt: Column<Instant?> = timestamp("CREATED_AT").nullable()
}