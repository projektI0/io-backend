package pl.edu.agh.auth

import io.ktor.server.application.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pl.edu.agh.auth.service.TokenCreationService
import pl.edu.agh.auth.service.UserService
import pl.edu.agh.auth.service.UserServiceImpl
import pl.edu.agh.auth.service.getJWTConfig

object AuthModule {

    fun Application.getKoinAuthModule() =
        module {
            single { getJWTConfig() }
            singleOf(::TokenCreationService)
            single<UserService> { UserServiceImpl(get()) }
        }
}