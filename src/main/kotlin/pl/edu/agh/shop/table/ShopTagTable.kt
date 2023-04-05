package pl.edu.agh.shop.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.shopId
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.tag.domain.tagId

class ShopTagTable : Table("SHOP_TAG") {
    val shopId: Column<ShopId> = shopId("SHOP_ID")
    val tagId: Column<TagId> = tagId("TAG_ID")
}
