package pl.edu.agh.tag

import io.ktor.server.application.*
import org.koin.dsl.module
import pl.edu.agh.tag.service.TagService
import pl.edu.agh.tag.service.TagServiceImpl

object AuthModule {

    fun Application.getKoinTagModule() =
        module {
            single<TagService> { TagServiceImpl() }
        }
}
