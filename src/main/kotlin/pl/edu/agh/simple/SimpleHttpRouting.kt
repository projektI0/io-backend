package pl.edu.agh.simple

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.SimpleHttpRouting() {
    routing {
        get("/{id}") {
            val maybeId = call.parameters["id"]
            call.respond("hello $maybeId")
        }
        get("/") {
            call.respond("hello")
        }
    }
}