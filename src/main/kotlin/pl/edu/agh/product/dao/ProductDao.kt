package pl.edu.agh.product.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.dto.ProductTableDTO
import pl.edu.agh.product.domain.request.ProductFilterRequest
import pl.edu.agh.product.table.ProductTable
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.utils.DBQueryResponseWithCount
import pl.edu.agh.utils.GINUtils.selectTS

object ProductDao {
    private fun userIdCondition(userId: LoginUserId): Op<Boolean> =
        ProductTable.generatedByUserId.isNull() or (ProductTable.generatedByUserId eq userId)

    fun getAllProducts(limit: Int, offset: Long, userId: LoginUserId): List<ProductTableDTO> =
        ProductTable
            .select {
                userIdCondition(userId)
            }
            .limit(limit, offset = offset)
            .map { ProductTable.toDomain(it) }

    fun getFilteredProducts(
        request: ProductFilterRequest,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Transaction.() -> DBQueryResponseWithCount<ProductTableDTO> = {
        val queryCondition = when (request.query) {
            "" -> Op.nullOp<Boolean>().isNull()
            else -> Op.build { ProductTable.ts.selectTS(listOf(request.query)) }
        }
        val tagsCondition = when {
            request.tags.isEmpty() -> Op.nullOp<Boolean>().isNull()
            else -> ProductTagTable.tagId.inList(request.tags)
        }
        val mainQuery = ProductTable.join(ProductTagTable, JoinType.INNER, ProductTable.id, ProductTagTable.productId)
            .slice(ProductTable.columns)
            .select {
                userIdCondition(userId) and tagsCondition and queryCondition
            }.withDistinct(true)

        val data = mainQuery
            .limit(limit, offset)
            .map { ProductTable.toDomain(it) }
        val count = mainQuery.count()

        DBQueryResponseWithCount(data, count)
    }

    fun getProduct(id: ProductId, userId: LoginUserId): Option<ProductTableDTO> =
        ProductTable
            .select {
                userIdCondition(userId) and (ProductTable.id eq id)
            }
            .firstOrNone()
            .map { ProductTable.toDomain(it) }

    fun insertNewProduct(name: String, description: String, userId: LoginUserId): Option<ProductTableDTO> {
        val newProductId = ProductTable.insert {
            it[ProductTable.name] = name
            it[ProductTable.description] = description
            it[ProductTable.generatedByUserId] = userId
        } get ProductTable.id

        return getProduct(newProductId, userId)
    }
}
