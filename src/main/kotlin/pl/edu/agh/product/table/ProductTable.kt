package pl.edu.agh.product.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.loginUserId
import pl.edu.agh.product.domain.ProductTableDTO
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.ProductView
import pl.edu.agh.product.domain.productId

object ProductTable : Table("PRODUCT") {
    val id: Column<ProductId> = productId("ID").autoIncrement()
    val name: Column<String> = varchar("NAME", 255)
    val description: Column<String> = varchar("DESCRIPTION", 255)
    val ts: Column<String> = varchar("ts", 255)
    val generatedByUserId: Column<LoginUserId?> = loginUserId("GENERATED_BY_USER_ID").nullable()

    fun toDomainView(it: ResultRow): ProductView {
        return ProductView(
            id = it[id], name = it[name], description = it[description], tags = emptyList()
        )
    }

    fun toDomain(rs: ResultRow): ProductTableDTO {
        return ProductTableDTO(id = rs[id], name = rs[name], description = rs[description])
    }
}