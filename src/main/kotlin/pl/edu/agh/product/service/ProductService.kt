package pl.edu.agh.product.service

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import io.ktor.http.*
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.product.dao.ProductDao
import pl.edu.agh.product.domain.ProductTableDTO
import pl.edu.agh.product.domain.ProductData
import pl.edu.agh.product.domain.ProductFilterRequest
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.utils.DBQueryResponseWithCount
import pl.edu.agh.utils.DomainException
import pl.edu.agh.utils.Transactor

class PaginationError(productFilterRequest: ProductFilterRequest) : DomainException(
    HttpStatusCode.BadRequest,
    "Something horribly wrong with $productFilterRequest",
    "Something went wrong while making pagination request"
)

class ProductCreationError(name: String, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.BadRequest,
        "Could not create product $name for user $userId",
        "Could not create product $name for user $userId"
    )

class ProductNotFound(productId: ProductId, userId: LoginUserId) :
    DomainException(HttpStatusCode.NotFound, "Product $productId not found for user $userId", "Product $productId not found for user $userId")


interface ProductService {
    fun getProduct(productId: ProductId, userId: LoginUserId): Effect<ProductNotFound, ProductTableDTO>
    fun createProduct(productData: ProductData, userId: LoginUserId): Effect<ProductCreationError, ProductTableDTO>
    suspend fun getAllProducts(limit: Int, offset: Long, userId: LoginUserId) : List<ProductTableDTO>
    fun getFilteredProducts(productFilterRequest: ProductFilterRequest, userId: LoginUserId): Effect<PaginationError, DBQueryResponseWithCount<ProductTableDTO>>
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
    override fun getFilteredProducts(productFilterRequest: ProductFilterRequest, userId: LoginUserId): Effect<PaginationError, DBQueryResponseWithCount<ProductTableDTO>> =
        effect {
            Either.conditionally(
                test = productFilterRequest.limit <= 0 || productFilterRequest.offset < 0,
                ifTrue = { PaginationError(productFilterRequest) },
                ifFalse = {
                    Transactor.dbQuery {
                        ProductDao.getFilteredProducts(
                            NonEmptyList.fromList(productFilterRequest.names),
                            productFilterRequest.limit,
                            productFilterRequest.offset,
                            userId
                        )()
                    }
                }).swap().bind()
        }
    override fun createProduct(productData: ProductData, userId: LoginUserId): Effect<ProductCreationError, ProductTableDTO> = effect {
        Transactor.dbQuery {
            ProductDao
                .insertNewProduct(productData.name, productData.description, userId)
                .bind {
                    ProductCreationError(productData.name, userId)
                }
        }
    }
}