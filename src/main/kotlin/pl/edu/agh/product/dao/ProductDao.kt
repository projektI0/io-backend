package pl.edu.agh.product.dao

import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.table.UserTable
import pl.edu.agh.product.domain.ProductTableDTO
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.table.ProductTable
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.shop.table.ShopTable
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
        productNames: Option<NonEmptyList<String>>,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Transaction.() -> DBQueryResponseWithCount<ProductTableDTO> = {
        productNames.fold(
            ifEmpty = {
                val mainQuery = ProductTable.select {
                    userIdCondition(userId)
                }

                val data = mainQuery
                    .limit(limit, offset)
                    .map { ProductTable.toDomain(it) }
                val count = mainQuery.count()

                DBQueryResponseWithCount(data, count)
            },
            ifSome = { names ->
                val mainQuery =
                    ProductTable.join(ProductTagTable, JoinType.LEFT, ProductTable.id, ProductTagTable.productId)
                        .select {
                            userIdCondition(userId) and ProductTable.ts.selectTS(names)
                        }

                val count = mainQuery.count()
                val data = mainQuery
                    .limit(limit, offset)
                    .map { ProductTable.toDomain(it) }

                DBQueryResponseWithCount(data, count)
            }
        )
    }
    fun getProduct(id: ProductId, userId: LoginUserId): Option<ProductTableDTO> =
        ProductTable
            .select{
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