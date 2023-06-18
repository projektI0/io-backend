package pl.edu.agh.shop.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.loginUserId
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.shopId

object BlacklistShopTable : Table("BLACKLIST_SHOP") {
    val shopId: Column<ShopId> = shopId("SHOP_ID")
    val userId: Column<LoginUserId> = loginUserId("USER_ID")
}
