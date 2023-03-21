package pl.edu.agh.shoppingList.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.shopId
import pl.edu.agh.shoppingList.domain.ShoppingListId
import pl.edu.agh.shoppingList.domain.shoppingListId

object ShoppingListShopTable : Table("SHOPPING_LIST_SHOP") {
    val shoppingListId: Column<ShoppingListId> = shoppingListId("SHOPPING_LIST_ID")
    val shopId: Column<ShopId> = shopId("SHOP_ID")
    val stopId: Column<Int> = integer("STOP_ID")
}