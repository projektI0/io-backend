package pl.edu.agh.path.service

import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.path.dao.PathDao
import pl.edu.agh.path.domain.PathRequest
import pl.edu.agh.path.domain.PathResponse
import pl.edu.agh.shop.dao.ShopDao
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import kotlin.math.sqrt

class Point(private val x: Double, private val y: Double) {
    fun distance(otherPoint: Point): Double {
        val dx = x - otherPoint.x
        val dy = y - otherPoint.y
        return sqrt(dx * dx + dy * dy)
    }
}

interface PathService {
    suspend fun findOptimalRoute(pathRequest: PathRequest, userId: LoginUserId): PathResponse
}

class PathServiceImpl : PathService {
    override suspend fun findOptimalRoute(pathRequest: PathRequest, userId: LoginUserId): PathResponse {
        val shoppingListId = pathRequest.shoppingListId
        val startX = pathRequest.longitude
        val startY = pathRequest.latitude
        val shopsIds =
            ShopDao.getAllShopsWithinBounds(startY - 5, startX - 5, startY + 5, startX + 5, userId).map { it.id }
                .toSet()
        // Create a set of all categories we need to buy
        val remainingTags = PathDao.getAllTopTagsForList(shoppingListId)

        // Create a map of the shops by tags
        val shopsByTags = PathDao.getShopsForTags(shopsIds, remainingTags)

        // Create a map of the tags by shops
        val tagsByShops = PathDao.getTagsForShops(shopsIds)

        // Initialize the starting point as the current location
        var currentPoint = Point(startX, startY)

        // Create a list to store the shops we need to visit
        val route = mutableListOf<ShopTableDTO>()

        while (remainingTags.isNotEmpty()) {
            // Find the nearest shop that sells one of the remaining categories
            val nearestShop = shopsByTags.filterKeys { remainingTags.contains(it) }.values.flatten()
                .minByOrNull { currentPoint.distance(Point(it.longitude, it.latitude)) }

            if (nearestShop is ShopTableDTO) {
                // Update current location and remove the category from the set of remaining categories
                currentPoint = Point(nearestShop.longitude, nearestShop.latitude)
                tagsByShops[nearestShop]?.forEach {
                    remainingTags.remove(it)
                }

                // Add the nearest shop to the route
                route.add(nearestShop)
            } else {
                // No more shops selling the remaining categories
                break
            }
        }

        return PathResponse(route, remainingTags)
    }
}
