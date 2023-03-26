package pl.edu.agh.utils

import io.ktor.http.*

object DomainExceptionLogger {
    val logger by LoggerDelegate()
}

open class DomainException(val httpStatusCode: HttpStatusCode, val internalMessage: String, val userMessage: String) {
    fun toResponsePairLogging(): Pair<HttpStatusCode, String> {
        DomainExceptionLogger.logger.warn(internalMessage)
        return httpStatusCode to userMessage
    }
}
