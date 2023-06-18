package pl.edu.agh.shop.service

import arrow.core.Either
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import arrow.core.continuations.either
import io.ktor.http.HttpStatusCode
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shop.dao.ShopDao
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.shop.domain.dto.ShopMapDTO
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.shop.domain.request.ShopRequest
import pl.edu.agh.shop.domain.request.ShopsBoundsRequest
import pl.edu.agh.utils.DomainException
import pl.edu.agh.utils.Transactor

class ShopCreationError(name: String, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.BadRequest,
        "Could not create shop $name for user $userId",
        "Could not create shop $name for user $userId"
    )

class ShopAlreadyOnBlacklist(shopId: ShopId, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.NotFound,
        "Shop $shopId is already on user $userId blacklist",
        "Shop $shopId is already on user $userId blacklist"
    )

class ShopNotFoundOnBlacklist(shopId: ShopId, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.NotFound,
        "Shop $shopId is not present on user $userId blacklist",
        "Shop $shopId is not present on user $userId blacklist"
    )

class ShopNotFoundError(shopId: ShopId, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.NotFound,
        "Shop $shopId not found for user $userId",
        "Shop $shopId not found for user $userId"
    )

interface ShopService {
    fun getShop(shopId: ShopId, userId: LoginUserId): Effect<ShopNotFoundError, ShopTableDTO>
    fun createShop(shopRequest: ShopRequest, userId: LoginUserId): Effect<ShopCreationError, ShopTableDTO>
    suspend fun getAllShops(limit: Int, offset: Long, userId: LoginUserId): List<ShopTableDTO>
    suspend fun getAllShopsWithinBounds(shopsBoundsRequest: ShopsBoundsRequest, userId: LoginUserId): List<ShopMapDTO>
    suspend fun addShopToUserBlacklist(shopId: ShopId, userId: LoginUserId): Effect<ShopAlreadyOnBlacklist, Unit>
    suspend fun removeShopFromUserBlacklist(shopId: ShopId, userId: LoginUserId): Effect<ShopNotFoundOnBlacklist, Unit>
}

class ShopServiceImpl : ShopService {

    private suspend fun secureGetShopFromBlacklist(
        shopId: ShopId,
        userId: LoginUserId
    ): Either<ShopNotFoundOnBlacklist, Pair<ShopId, LoginUserId>> = either {
        ShopDao
            .secureGetShopFromBlacklist(shopId, userId)
            .bind { ShopNotFoundOnBlacklist(shopId, userId) }
    }

    private suspend fun secureCheckBlacklist(
        shopId: ShopId,
        userId: LoginUserId
    ): Either<ShopAlreadyOnBlacklist, Unit> = either {
        ShopDao.secureGetShopFromBlacklist(shopId, userId).map { ShopAlreadyOnBlacklist(shopId, userId) }
    }

    override fun getShop(shopId: ShopId, userId: LoginUserId): Effect<ShopNotFoundError, ShopTableDTO> =
        effect {
            Transactor.dbQuery {
                ShopDao.getShop(shopId, userId)
                    .bind {
                        ShopNotFoundError(shopId, userId)
                    }
            }
        }

    override suspend fun getAllShops(limit: Int, offset: Long, userId: LoginUserId): List<ShopTableDTO> =
        Transactor.dbQuery { ShopDao.getAllShops(limit, offset, userId) }

    override suspend fun getAllShopsWithinBounds(
        shopsBoundsRequest: ShopsBoundsRequest,
        userId: LoginUserId
    ): List<ShopMapDTO> =
        Transactor.dbQuery {
            ShopDao.getAllShopsWithinBounds(
                shopsBoundsRequest.lowerLeftLat,
                shopsBoundsRequest.lowerLeftLng,
                shopsBoundsRequest.upperRightLat,
                shopsBoundsRequest.upperRightLng,
                userId
            )
        }

    override suspend fun addShopToUserBlacklist(
        shopId: ShopId,
        userId: LoginUserId
    ): Effect<ShopAlreadyOnBlacklist, Unit> =
        effect {
            Transactor.dbQuery {
                secureCheckBlacklist(shopId, userId).bind()
                ShopDao.addShopToUserBlacklist(shopId, userId)
            }
        }

    override suspend fun removeShopFromUserBlacklist(
        shopId: ShopId,
        userId: LoginUserId
    ): Effect<ShopNotFoundOnBlacklist, Unit> =
        effect {
            Transactor.dbQuery {
                secureGetShopFromBlacklist(shopId, userId).bind()
                ShopDao.removeShopFromUserBlacklist(shopId, userId)
            }
        }

    override fun createShop(
        shopRequest: ShopRequest,
        userId: LoginUserId
    ): Effect<ShopCreationError, ShopTableDTO> = effect {
        Transactor.dbQuery {
            ShopDao
                .insertNewShop(
                    shopRequest,
                    userId
                )
                .bind {
                    ShopCreationError(shopRequest.name, userId)
                }
        }
    }
}
