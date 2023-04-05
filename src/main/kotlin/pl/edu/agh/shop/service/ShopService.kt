package pl.edu.agh.shop.service

import pl.edu.agh.shop.dao.ShopDao
import pl.edu.agh.shop.domain.ShopTableDTO
import pl.edu.agh.shop.domain.ShopsBoundsRequest
import pl.edu.agh.utils.Transactor

interface ShopService {
    suspend fun getAllShops(): List<ShopTableDTO>
    suspend fun getAllShopsWithinBounds(shopsBoundsRequest: ShopsBoundsRequest): List<ShopTableDTO>
}

class ShopServiceImpl : ShopService {
    override suspend fun getAllShops(): List<ShopTableDTO> =
        Transactor.dbQuery { ShopDao.getAllShops() }

    override suspend fun getAllShopsWithinBounds(shopsBoundsRequest: ShopsBoundsRequest): List<ShopTableDTO> =
        Transactor.dbQuery {
            ShopDao.getAllShopsWithinBounds(
                shopsBoundsRequest.lowerLeftLat,
                shopsBoundsRequest.lowerLeftLng,
                shopsBoundsRequest.upperRightLat,
                shopsBoundsRequest.upperRightLng
            )
        }
}
