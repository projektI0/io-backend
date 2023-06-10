package pl.edu.agh.recipe.domain

import kotlinx.serialization.Serializable
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.tag.domain.TagId

/**
 * Request for adding, new customer's recipe
 */
@Serializable
data class RecipeRequest(
    val name: String,
    val description: String,
    val ingredients: List<ProductId>,
    val tags: List<TagId>
)
