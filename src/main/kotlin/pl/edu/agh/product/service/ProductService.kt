package pl.edu.agh.product.service

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import io.ktor.http.HttpStatusCode
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.product.dao.ProductDao
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.dto.ProductTableDTO
import pl.edu.agh.product.domain.request.ProductRequest
import pl.edu.agh.utils.DBQueryResponseWithCount
import pl.edu.agh.utils.DomainException
import pl.edu.agh.utils.Transactor

class PaginationError(limit: Int, offset: Long) : DomainException(
    HttpStatusCode.BadRequest,
    "Something horribly wrong with searching products: limit - $limit, offset - $offset",
    "Something went wrong while making pagination request"
)

class ProductCreationError(name: String, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.BadRequest,
        "Could not create product $name for user $userId",
        "Could not create product $name for user $userId"
    )

class ProductNotFound(productId: ProductId, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.NotFound,
        "Product $productId not found for user $userId",
        "Product $productId not found for user $userId"
    )

interface ProductService {
    fun getProduct(productId: ProductId, userId: LoginUserId): Effect<ProductNotFound, ProductTableDTO>
    fun createProduct(
        productRequest: ProductRequest,
        userId: LoginUserId
    ): Effect<ProductCreationError, ProductTableDTO>

    suspend fun getAllProducts(limit: Int, offset: Long, userId: LoginUserId): List<ProductTableDTO>
    fun getFilteredProducts(
        query: String,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Effect<PaginationError, DBQueryResponseWithCount<ProductTableDTO>>
}

class ProductServiceImpl : ProductService {
    override fun getProduct(productId: ProductId, userId: LoginUserId): Effect<ProductNotFound, ProductTableDTO> =
        effect {
            Transactor.dbQuery {
                ProductDao.getProduct(productId, userId)
                    .bind {
                        ProductNotFound(productId, userId)
                    }
            }
        }

    override suspend fun getAllProducts(limit: Int, offset: Long, userId: LoginUserId): List<ProductTableDTO> =
        Transactor.dbQuery {
            ProductDao.getAllProducts(limit, offset, userId)
        }

    override fun getFilteredProducts(
        query: String,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Effect<PaginationError, DBQueryResponseWithCount<ProductTableDTO>> =
        effect {
            Either.conditionally(
                test = limit <= 0 || offset < 0,
                ifTrue = { PaginationError(limit, offset) },
                ifFalse = {
                    Transactor.dbQuery {
                        ProductDao.getFilteredProducts(
                            NonEmptyList.fromList(listOf(query)),
                            limit,
                            offset,
                            userId
                        )()
                    }
                }
            ).swap().bind()
        }

    override fun createProduct(
        productRequest: ProductRequest,
        userId: LoginUserId
    ): Effect<ProductCreationError, ProductTableDTO> = effect {
        Transactor.dbQuery {
            ProductDao
                .insertNewProduct(productRequest.name, productRequest.description, userId)
                .bind {
                    ProductCreationError(productRequest.name, userId)
                }
        }
    }
}
