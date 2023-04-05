package pl.edu.agh.product.service

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import io.ktor.http.HttpStatusCode
import pl.edu.agh.product.dao.ProductDao
import pl.edu.agh.product.domain.Product
import pl.edu.agh.product.domain.ProductFilterRequest
import pl.edu.agh.utils.DBQueryResponseWithCount
import pl.edu.agh.utils.DomainException
import pl.edu.agh.utils.Transactor

class PaginationError(productFilterRequest: ProductFilterRequest) : DomainException(
    HttpStatusCode.BadRequest,
    "Something horribly wrong with $productFilterRequest",
    "Something went wrong while making pagination request"
)

interface ProductService {
    fun getProducts(productFilterRequest: ProductFilterRequest): Effect<PaginationError, DBQueryResponseWithCount<Product>>
}

class ProductServiceImpl : ProductService {
    override fun getProducts(productFilterRequest: ProductFilterRequest): Effect<PaginationError, DBQueryResponseWithCount<Product>> =
        effect {
            Either.conditionally(
                test = productFilterRequest.limit <= 0 || productFilterRequest.offset < 0,
                ifTrue = { PaginationError(productFilterRequest) },
                ifFalse = {
                    Transactor.dbQuery {
                        ProductDao.getProducts(
                            NonEmptyList.fromList(productFilterRequest.names),
                            productFilterRequest.limit,
                            productFilterRequest.offset
                        )()
                    }
                }
            ).swap().bind()
        }
}
