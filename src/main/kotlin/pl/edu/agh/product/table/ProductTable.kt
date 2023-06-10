package pl.edu.agh.product.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.loginUserId
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.dto.ProductTableDTO
import pl.edu.agh.product.domain.dto.ProductTagTableDTO
import pl.edu.agh.product.domain.productId
import pl.edu.agh.recipe.domain.RecipeDTO
import pl.edu.agh.recipe.table.RecipeIngredientTable
import pl.edu.agh.tag.domain.TagId

object ProductTable : Table("PRODUCT") {
    val id: Column<ProductId> = productId("ID").autoIncrement()
    val name: Column<String> = varchar("NAME", 255)
    val description: Column<String> = varchar("DESCRIPTION", 255)
    val ts: Column<String> = varchar("ts", 255)
    val generatedByUserId: Column<LoginUserId?> = loginUserId("GENERATED_BY_USER_ID").nullable()
    val isRecipe: Column<Boolean> = bool("IS_RECIPE")

    fun toDomainView(it: ResultRow): ProductTagTableDTO {
        return ProductTagTableDTO(
            ProductTableDTO(id = it[id], name = it[name], description = it[description]),
            tags = ProductTagTable.select(ProductTagTable.productId.eq(it[id])).map { it[ProductTagTable.tagId] }
                .toSet()
        )
    }

    fun toDomain(rs: ResultRow): ProductTableDTO {
        return ProductTableDTO(id = rs[id], name = rs[name], description = rs[description])
    }

    fun toDomainRecipe(rs: ResultRow, ingredients: List<ProductTableDTO>?, tags: Set<TagId>?): RecipeDTO {
        return RecipeDTO(
            recipeId = rs[id],
            name = rs[name],
            ingredients ?: RecipeIngredientTable.join(
                ProductTable,
                JoinType.INNER,
                RecipeIngredientTable.productId,
                id
            )
                .select(RecipeIngredientTable.recipeId eq rs[id])
                .map { toDomain(it) },
            tags ?: ProductTagTable
                .select(ProductTagTable.productId.eq(rs[id]))
                .map { it[ProductTagTable.tagId] }
                .toSet()
        )
    }
}
