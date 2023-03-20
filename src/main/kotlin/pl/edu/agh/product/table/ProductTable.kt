package pl.edu.agh.product.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.productId

object ProductTable : IdTable<ProductId>("PRODUCT") {
    override val id: Column<EntityID<ProductId>> = productId("ID").autoIncrement().entityId()
    val name: Column<String> = varchar("NAME", 255)
    val description: Column<String> = varchar("DESCRIPTION", 255)
}