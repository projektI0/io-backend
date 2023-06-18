package pl.edu.agh.path.service

import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.path.dao.PathDao
import pl.edu.agh.path.domain.PathRequest
import pl.edu.agh.path.domain.PathResponse
import pl.edu.agh.shop.domain.dto.ShopMapDTO
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.tag.domain.TagId
import kotlin.math.sqrt
import kotlin.streams.toList

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
            PathDao.getAllPossibleShopsWithinBounds(startY - 5, startX - 5, startY + 5, startX + 5, userId)
                .map { it.id }
                .toSet()
        // Create a set of all categories we need to buy
        val allTags: MutableSet<TagId> = PathDao.getAllTopTagsForList(shoppingListId)

        // Create a map of the shops by tags
        val shopsByTags: Map<TagId, Set<ShopTableDTO>> = PathDao.getShopsForTags(shopsIds, allTags)

        // Create a map of the tags by shops
        val tagsByShops: Map<ShopTableDTO, Set<TagId>> = PathDao.getTagsForShops(shopsIds)

        val (availableTags, remainingTags) = allTags.partition { shopsByTags.containsKey(it) }

        // Initialize the starting point as the current location
        val currentPoint = Point(startX, startY)

        val route: MutableList<ShopTableDTO> = if (pathRequest.fewestShops == true) {
            fewestShopsPath(tagsByShops, currentPoint, availableTags.toMutableSet())
        } else {
            shortestPath(shopsByTags, tagsByShops, currentPoint, availableTags.toMutableSet())
        }

        return PathResponse(
            route.stream().map { ShopMapDTO(it.id, it.name, it.longitude, it.latitude, it.address, false) }.toList(),
            remainingTags.toSet()
        )
    }

    private fun shortestPath(
        shopsByTags: Map<TagId, Set<ShopTableDTO>>,
        tagsByShops: Map<ShopTableDTO, Set<TagId>>,
        startingPoint: Point,
        tags: MutableSet<TagId>
    ): MutableList<ShopTableDTO> {
        val path: MutableList<ShopTableDTO>
        val pointPath: MutableList<Point>

        val shops: MutableSet<ShopTableDTO> = tags.flatMap { tag ->
            shopsByTags[tag] ?: emptySet()
        }.toMutableSet()

        // find all shops that need to be included (one of a kind)
        val necessaryShops: MutableSet<ShopTableDTO> = mutableSetOf()
        shopsByTags.entries
            .filter { tags.contains(it.key) }
            .filter { it.value.size == 1 }
            .forEach { necessaryShops.addAll(it.value) }

        if (necessaryShops.isNotEmpty()) {
            val (sortedRoute, sortedPointRoute) = sortShops(startingPoint, necessaryShops)
            path = sortedRoute
            pointPath = sortedPointRoute
            shops.removeIf { necessaryShops.contains(it) }
        } else {
            val firstShop = shopClosestToPoint(startingPoint, shops)
            path = mutableListOf(firstShop)
            pointPath = mutableListOf(startingPoint, Point(firstShop.longitude, firstShop.latitude))
            shops.remove(firstShop)
        }

        path.forEach { shop -> tags.removeIf { !(tagsByShops[shop] ?: emptySet()).contains(it) } }

        while (tags.isNotEmpty()) {
            val (index, newShop) = findOptimalShop(pointPath, shops)
            val newShopTags = tagsByShops[newShop]

            path.add(index, newShop)
            pointPath.add(index, Point(newShop.longitude, newShop.latitude))

            // remove all doubles (new shop contains tags added to path earlier)
            val removeShopsAtIndex = mutableListOf<Int>()
            path.withIndex().forEach {
                if (newShopTags?.containsAll(tagsByShops[it.value] ?: emptySet()) == true && it.value != newShop) {
                    removeShopsAtIndex.add(it.index)
                }
            }

            removeShopsAtIndex.asReversed().forEach {
                path.removeAt(it)
                pointPath.removeAt(it)
            }

            tags.removeIf { newShopTags?.contains(it) ?: false }
        }

        return path
    }

    private fun sortShops(
        startingPoint: Point,
        shops: MutableSet<ShopTableDTO>
    ): Pair<MutableList<ShopTableDTO>, MutableList<Point>> {
        val path: MutableList<ShopTableDTO> = mutableListOf()
        val pointPath = mutableListOf(startingPoint)

        val startingShop = shopClosestToPoint(startingPoint, shops)
        path.add(startingShop)
        pointPath.add(Point(startingShop.longitude, startingShop.latitude))

        while (shops.isNotEmpty()) {
            val (index, shop) = findOptimalShop(pointPath, shops)
            path.add(index, shop)
            pointPath.add(index, Point(shop.longitude, shop.latitude))
            shops.remove(shop)
        }

        return Pair(path, pointPath)
    }

    private fun fewestShopsPath(
        tagsByShops: Map<ShopTableDTO, Set<TagId>>,
        startingPoint: Point,
        tags: MutableSet<TagId>
    ): MutableList<ShopTableDTO> {
        val route = mutableListOf<ShopTableDTO>()
        val pointRoute = mutableListOf(startingPoint)

        while (tags.isNotEmpty()) {
            val bestShops = shopsWithMostTags(tagsByShops, tags)

            if (bestShops.isNotEmpty()) {
                val (index, shop) = findOptimalShop(pointRoute, bestShops)
                route.add(index, shop)
                pointRoute.add(index, Point(shop.longitude, shop.latitude))
                tags.removeIf { !(tagsByShops[shop] ?: emptySet()).contains(it) }
            } else {
                break
            }
        }
        return route
    }

    private fun findOptimalShop(currentPath: List<Point>, potentialShops: Set<ShopTableDTO>): Pair<Int, ShopTableDTO> {
        var minDistance = Double.MAX_VALUE
        var bestShop = potentialShops.first()
        var indexToInsert = 1

        if (currentPath.size == 1) {
            bestShop = shopClosestToPoint(currentPath[0], potentialShops)
        } else {
            for (potential in potentialShops) {
                for ((index, point) in currentPath.withIndex()) {
                    if (index == 0) continue

                    var currentDistance = distanceFromShopToPoint(potential, point)
                    currentDistance += distanceFromShopToPoint(potential, currentPath[index - 1])

                    if (currentDistance < minDistance) {
                        indexToInsert = index
                        minDistance = currentDistance
                        bestShop = potential
                    }
                }
            }
        }

        return Pair(indexToInsert, bestShop)
    }

    private fun shopClosestToPoint(point: Point, shops: Set<ShopTableDTO>): ShopTableDTO {
        return shops.minBy { point.distance(Point(it.longitude, it.latitude)) }
    }

    private fun distanceFromShopToPoint(shop: ShopTableDTO, point: Point): Double {
        return point.distance(Point(shop.longitude, shop.latitude))
    }

    private fun shopsWithMostTags(tagsByShops: Map<ShopTableDTO, Set<TagId>>, remainingTags: MutableSet<TagId>):
        MutableSet<ShopTableDTO> {
        var maxTags = 0
        var maxTagsShops = mutableSetOf<ShopTableDTO>()

        for (entry in tagsByShops.entries.iterator()) {
            val noOfTags = entry.value.filter { remainingTags.contains(it) }.size
            if (noOfTags > maxTags) {
                maxTags = noOfTags
                maxTagsShops = mutableSetOf(entry.key)
            } else if (noOfTags == maxTags) {
                maxTagsShops.add(entry.key)
            }
        }
        return maxTagsShops
    }
}
