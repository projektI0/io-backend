package pl.edu.agh.shoppingList.table

import arrow.core.toOption
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.loginUserId
import pl.edu.agh.shoppingList.domain.ShoppingList
import pl.edu.agh.shoppingList.domain.shoppingListId
import java.time.Instant

object ShoppingListTable : Table("SHOPPING_LIST") {
    val id = shoppingListId("ID").autoIncrement()
    val name: Column<String?> = varchar("NAME", 256).nullable()
    val ownerId: Column<LoginUserId> = loginUserId("OWNER_ID")
    val createdAt: Column<Instant> = timestamp("CREATED_AT").default(Instant.now())

    fun toDomain(it: ResultRow) = ShoppingList(
        id = it[id],
        name = it[name].toOption(),
        ownerId = it[ownerId],
        createdAt = it[createdAt]
    )
}
