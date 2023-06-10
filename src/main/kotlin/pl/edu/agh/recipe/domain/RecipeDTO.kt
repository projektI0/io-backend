package pl.edu.agh.recipe.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.dto.ProductTableDTO
import pl.edu.agh.tag.domain.TagId

@Serializable
data class RecipeDTO(
    val recipeId: ProductId,
    val name: String,
    val ingredients: List<ProductTableDTO>,
    val tags: Set<TagId>
)
