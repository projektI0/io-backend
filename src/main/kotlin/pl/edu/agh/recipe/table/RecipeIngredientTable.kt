package pl.edu.agh.recipe.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.productId

object RecipeIngredientTable : Table("RECIPE_INGREDIENT") {
    val recipeId: Column<ProductId> = productId("RECIPE_ID")
    val productId: Column<ProductId> = productId("PRODUCT_ID")
}
