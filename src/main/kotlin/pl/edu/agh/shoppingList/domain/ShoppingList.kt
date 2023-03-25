package pl.edu.agh.shoppingList.domain

import arrow.core.Option
import kotlinx.serialization.Serializable
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.utils.InstantSerializer
import pl.edu.agh.utils.OptStringSerializer
import java.time.Instant

@Serializable
class ShoppingList(
    val id: ShoppingListId,
    @Serializable(with = OptStringSerializer::class)
    val name: Option<String>,
    val ownerId: LoginUserId,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant
)