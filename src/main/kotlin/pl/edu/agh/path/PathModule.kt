package pl.edu.agh.path

import io.ktor.server.application.Application
import org.koin.dsl.module
import pl.edu.agh.path.service.PathService
import pl.edu.agh.path.service.PathServiceImpl

object PathModule {
    fun Application.getKoinPathModule() =
        module {
            single<PathService> { PathServiceImpl() }
        }
}
