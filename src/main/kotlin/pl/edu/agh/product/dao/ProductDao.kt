package pl.edu.agh.product.dao

import arrow.core.Option
import arrow.core.firstOrNone
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.product.domain.ProductId
import pl.edu.agh.product.domain.dto.ProductTableDTO
import pl.edu.agh.product.domain.request.ProductFilterRequest
import pl.edu.agh.product.domain.request.ProductRequest
import pl.edu.agh.product.table.ProductTable
import pl.edu.agh.product.table.ProductTagTable
import pl.edu.agh.recipe.domain.RecipeDTO
import pl.edu.agh.recipe.domain.RecipeRequest
import pl.edu.agh.recipe.table.RecipeIngredientTable
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.tag.table.TagTable
import pl.edu.agh.utils.DBQueryResponseWithCount
import pl.edu.agh.utils.GINUtils.selectTS

object ProductDao {
    private fun userIdCondition(userId: LoginUserId): Op<Boolean> =
        ProductTable.generatedByUserId.isNull() or (ProductTable.generatedByUserId eq userId)

    private fun queryCondition(query: String): Op<Boolean> = when (query) {
        "" -> Op.nullOp<Boolean>().isNull()
        else -> Op.build { ProductTable.ts.selectTS(listOf(query)) }
    }

    private fun tagsCondition(tags: List<TagId>): Op<Boolean> = when {
        tags.isEmpty() -> Op.nullOp<Boolean>().isNull()
        else -> ProductTagTable.tagId.inList(tags)
    }

    fun getAllProducts(limit: Int, offset: Long, userId: LoginUserId): List<ProductTableDTO> =
        ProductTable
            .select {
                userIdCondition(userId)
            }
            .limit(limit, offset = offset)
            .map { ProductTable.toDomain(it) }

    fun getProduct(id: ProductId, userId: LoginUserId): Option<ProductTableDTO> =
        ProductTable
            .select {
                userIdCondition(userId) and (ProductTable.id eq id)
            }
            .firstOrNone()
            .map { ProductTable.toDomain(it) }

    fun getFilteredProducts(
        request: ProductFilterRequest,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Transaction.() -> DBQueryResponseWithCount<ProductTableDTO> = {
        val mainQuery = ProductTable.join(ProductTagTable, JoinType.INNER, ProductTable.id, ProductTagTable.productId)
            .slice(ProductTable.columns)
            .select {
                userIdCondition(userId) and tagsCondition(request.tags) and queryCondition(request.query) and (ProductTable.isRecipe eq false)
            }.withDistinct(true)

        val data = mainQuery
            .limit(limit, offset)
            .map { ProductTable.toDomain(it) }
        val count = mainQuery.count()

        DBQueryResponseWithCount(data, count)
    }

    fun insertNewProduct(request: ProductRequest, userId: LoginUserId): Option<ProductTableDTO> {
        val newProductId = insertProduct(request.name, request.description, userId, false)

        insertProductTags(newProductId, request.tags)

        return getProduct(newProductId, userId)
    }

    fun getAllRecipes(userId: LoginUserId): List<RecipeDTO> = wrapWithRecipes(
        ProductTable.join(ProductTagTable, JoinType.INNER, ProductTable.id, ProductTagTable.productId)
            .select {
                userIdCondition(userId) and ProductTable.isRecipe.eq(true)
            }
    )

    fun getRecipe(id: ProductId, userId: LoginUserId): Option<RecipeDTO> {
        return ProductTable
            .select {
                (ProductTable.id eq id) and userIdCondition(userId) and ProductTable.isRecipe.eq(true)
            }.firstOrNone().map { ProductTable.toDomainRecipe(it, null, null) }
    }

    fun getFilteredRecipes(
        request: ProductFilterRequest,
        limit: Int,
        offset: Long,
        userId: LoginUserId
    ): Transaction.() -> List<RecipeDTO> = {
        val mainQuery = ProductTable.join(ProductTagTable, JoinType.INNER, ProductTable.id, ProductTagTable.productId)
            .select {
                userIdCondition(userId) and tagsCondition(request.tags) and queryCondition(request.query) and (ProductTable.isRecipe eq true)
            }.withDistinct(true)

        wrapWithRecipes(mainQuery.limit(limit, offset))
    }

    fun insertNewRecipe(request: RecipeRequest, userId: LoginUserId): Option<RecipeDTO> {
        val newProductId = insertProduct(request.name, request.description, userId, true)

        RecipeIngredientTable.batchInsert(
            request.ingredients
        ) {
            this[RecipeIngredientTable.recipeId] = newProductId
            this[RecipeIngredientTable.productId] = it
        }

        insertProductTags(newProductId, request.tags)

        return getRecipe(newProductId, userId)
    }

    private fun insertProductTags(productId: ProductId, tags: List<TagId>) = ProductTagTable.batchInsert(
        TagTable.select { TagTable.id.inList(tags) }
            .map { Pair(it[TagTable.id], it[TagTable.parentTagId] ?: it[TagTable.id]) }
    ) {
        this[ProductTagTable.productId] = productId
        this[ProductTagTable.tagId] = it.first
        this[ProductTagTable.mainTagId] = it.second
    }

    private fun insertProduct(name: String, description: String, userId: LoginUserId, isRecipe: Boolean): ProductId =
        ProductTable.insert {
            it[ProductTable.name] = name
            it[ProductTable.description] = description
            it[ProductTable.generatedByUserId] = userId
            it[ProductTable.isRecipe] = isRecipe
        } get ProductTable.id

    private fun wrapWithRecipes(query: Query): List<RecipeDTO> {
        val recipes = query
            .groupBy { it[ProductTable.id] }
            .map { (id, rows) ->
                Triple(
                    id,
                    rows[0][ProductTable.name],
                    rows.map { it[ProductTagTable.tagId] }.toSet()
                )
            }

        val ingredients =
            RecipeIngredientTable.join(ProductTable, JoinType.INNER, RecipeIngredientTable.productId, ProductTable.id)
                .select(RecipeIngredientTable.recipeId.inList(recipes.map { it.first }))
                .groupBy { it[RecipeIngredientTable.recipeId] }
                .mapValues { (_, rows) -> rows.map { ProductTable.toDomain(it) } }

        return recipes.map { RecipeDTO(it.first, it.second, ingredients.getOrDefault(it.first, emptyList()), it.third) }
    }
}
