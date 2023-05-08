package pl.edu.agh.path.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.shop.domain.dto.ShopTableDTO
import pl.edu.agh.tag.domain.TagId

@Serializable
data class PathResponse(val shops: List<ShopTableDTO>, val remainingTags: Set<TagId>)
