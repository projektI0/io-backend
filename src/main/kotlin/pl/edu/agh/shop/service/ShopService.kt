package pl.edu.agh.shop.service

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import io.ktor.http.*
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.shop.dao.ShopDao
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.domain.ShopsBoundsRequest
import pl.edu.agh.shop.domain.ShopData
import pl.edu.agh.shop.domain.ShopId
import pl.edu.agh.utils.DomainException
import pl.edu.agh.utils.Transactor

class ShopCreationError(name: String, userId: LoginUserId) :
    DomainException(
        HttpStatusCode.BadRequest,
        "Could not create shop $name for user $userId",
        "Could not create shop $name for user $userId"
    )

class ShopNotFoundError(shopId: ShopId, userId: LoginUserId) :
    DomainException(HttpStatusCode.NotFound, "Shop $shopId not found for user $userId", "Shop $shopId not found for user $userId")

interface ShopService {
    fun getShop(shopId: ShopId, userId: LoginUserId): Effect<ShopNotFoundError, ShopTableDTO>
    fun createShop(shopData: ShopData, userId: LoginUserId): Effect<ShopCreationError, ShopTableDTO>
    suspend fun getAllShops(limit: Int, offset: Long, userId: LoginUserId): List<ShopTableDTO>
    suspend fun getAllShopsWithinBounds(shopsBoundsRequest: ShopsBoundsRequest, userId: LoginUserId): List<ShopTableDTO>
}

class ShopServiceImpl : ShopService {
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
        Transactor.dbQuery { ShopDao.getAllShops(limit, offset, userId)}

    override suspend fun getAllShopsWithinBounds(shopsBoundsRequest: ShopsBoundsRequest, userId: LoginUserId) : List<ShopTableDTO> =
        Transactor.dbQuery {
            ShopDao.getAllShopsWithinBounds(
                shopsBoundsRequest.lowerLeftLat,
                shopsBoundsRequest.lowerLeftLng,
                shopsBoundsRequest.upperRightLat,
                shopsBoundsRequest.upperRightLng,
                userId
            )
        }

    override fun createShop(shopData: ShopData, userId: LoginUserId): Effect<ShopCreationError, ShopTableDTO> = effect {
        Transactor.dbQuery {
            ShopDao
                .insertNewShop(shopData.name, shopData.longitude, shopData.latitude, shopData.address, userId)
                .bind {
                    ShopCreationError(shopData.name, userId)
                }
        }
    }
}