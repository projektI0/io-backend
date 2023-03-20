package pl.edu.agh.product.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.productId
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.tag.domain.tagId

object ProductTagTable : Table("PRODUCT_TAG") {
    val productId: Column<ProductId> = productId("PRODUCT_ID")
    val tagId: Column<TagId> = tagId("TAG_ID")
}