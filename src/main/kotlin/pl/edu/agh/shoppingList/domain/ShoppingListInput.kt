package pl.edu.agh.shoppingList.domain

import pl.edu.agh.auth.domain.LoginUserId

data class ShoppingListInput(val userId: LoginUserId, val name: String)
