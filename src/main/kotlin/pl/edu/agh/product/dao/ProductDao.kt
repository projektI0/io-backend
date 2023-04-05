package pl.edu.agh.product.dao

import arrow.core.NonEmptyList
import arrow.core.Option
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import pl.edu.agh.product.domain.Product
import pl.edu.agh.product.table.ProductTable
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.utils.DBQueryResponseWithCount
import pl.edu.agh.utils.GINUtils.selectTS

object ProductDao {

    fun getProducts(
        productNames: Option<NonEmptyList<String>>,
        limit: Int,
        offset: Long
    ): Transaction.() -> DBQueryResponseWithCount<Product> = {
        productNames.fold(
            ifEmpty = {
                val mainQuery = ProductTable.selectAll()

                val data = mainQuery
                    .limit(limit, offset)
                    .map { ProductTable.toDomain(it) }
                val count = mainQuery.count()

                DBQueryResponseWithCount(data, count)
            },
            ifSome = { names ->
                val mainQuery =
                    ProductTable.join(ProductTagTable, JoinType.LEFT, ProductTable.id, ProductTagTable.productId)
                        .select { ProductTable.ts.selectTS(names) }

                val count = mainQuery.count()
                val data = mainQuery
                    .limit(limit, offset)
                    .map { ProductTable.toDomain(it) }

                DBQueryResponseWithCount(data, count)
            }
        )
    }
}
