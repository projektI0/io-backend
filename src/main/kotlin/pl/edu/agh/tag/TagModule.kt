package pl.edu.agh.tag

import io.ktor.server.application.Application
import org.koin.dsl.module
import pl.edu.agh.tag.service.TagService
import pl.edu.agh.tag.service.TagServiceImpl

object TagModule {
    fun Application.getKoinTagModule() =
        module {
            single<TagService> { TagServiceImpl() }
        }
}
