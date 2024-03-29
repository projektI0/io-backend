package pl.edu.agh.utils

import arrow.core.Either
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import arrow.core.continuations.either
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.slf4j.LoggerFactory

object DatabaseConnector {
    fun initDB() {
        val ioBackendDocker: Boolean = System.getenv("IO_BACKEND_DOCKER")?.toBooleanStrictOrNull() ?: false
        val configPath = if (ioBackendDocker) "/dbresourcedocker.properties" else "/dbresource.properties"

        val dbConfig = HikariConfig(configPath)
        val dataSource = HikariDataSource(dbConfig)
        Database.connect(dataSource)
        LoggerFactory.getLogger(Application::class.simpleName).info("Initialized Database")
    }
}

object Transactor {
    private val logger by LoggerDelegate()

    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block(this) }

    suspend fun <L, R> dbQueryEffect(empty: L, block: suspend Transaction.() -> Either<L, R>): Effect<L, R> =
        effect {
            newSuspendedTransaction(Dispatchers.IO) {
                val caughtEither =
                    Either
                        .catch { block(this) }
                        .tapLeft {
                            logger.error("Rollback, unknown error (caught), ${it.message}", it)
                            rollback()
                        }
                        .mapLeft { empty }
                        .tap {
                            it.tapLeft {
                                logger.error("Rollback, user error $it")
                                rollback()
                            }
                        }
                either {
                    caughtEither.bind().bind()
                }
            }.bind()
        }

    suspend fun <T> transactional(block: suspend Transaction.() -> T): Deferred<T> {
        return suspendedTransactionAsync(Dispatchers.IO) {
            block(this)
        }
    }
}

object GINUtils {

    class TSExpression(private val columnName: String, expr: List<String>) : Op<Boolean>() {
        private val parsedExpr = VarCharColumnType().notNullValueToDB(expr.joinToString(" | ") { "$it:*" })

        override fun toQueryBuilder(queryBuilder: QueryBuilder): Unit =
            queryBuilder { append("$columnName @@ to_tsquery('english', '$parsedExpr')") }
    }

    fun Column<String>.selectTS(expr: List<String>): Op<Boolean> {
        return TSExpression(this.name, expr)
    }
}
