package pl.edu.agh.auth.service

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right
import org.jetbrains.exposed.sql.batchInsert
import pl.edu.agh.auth.dao.UserDao
import pl.edu.agh.auth.domain.LoginUserBasicData
import pl.edu.agh.auth.domain.LoginUserDTO
import pl.edu.agh.auth.domain.LoginUserData
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.table.UserRolesTable

enum class RegisterException {
    EMAIL_ALREADY_EXISTS,
    PASSWORD_TOO_SHORT
}

enum class LoginException {
    USER_NOT_FOUND,
    WRONG_PASSWORD
}

interface AuthService {
    suspend fun signUpNewUser(loginUserBasicData: LoginUserBasicData): Either<RegisterException, LoginUserData>
    suspend fun signInUser(loginUserBasicData: LoginUserBasicData): Either<LoginException, LoginUserData>
}


class AuthServiceImpl(private val tokenCreationService: TokenCreationService) : AuthService {

    override suspend fun signUpNewUser(loginUserBasicData: LoginUserBasicData): Either<RegisterException, LoginUserData> =
        either {
            Either.conditionally(
                loginUserBasicData.password.length > 8,
                ifFalse = { RegisterException.PASSWORD_TOO_SHORT },
                ifTrue = { })
                .bind()

            UserDao
                .findUserByEmail(loginUserBasicData.email)
                .map { RegisterException.EMAIL_ALREADY_EXISTS }
                .toEither { }
                .swap()
                .bind()

            val newId = UserDao.insertNewUser(loginUserBasicData)
            val basicRoles = listOf(Roles.USER)

            UserRolesTable.batchInsert(basicRoles) { role ->
                this[UserRolesTable.roleId] = role.id
                this[UserRolesTable.userId] = newId
            }

            LoginUserData(
                loginUserDTO = LoginUserDTO(id = newId, email = loginUserBasicData.email),
                roles = basicRoles,
                jwtToken = tokenCreationService.createToken(loginUserBasicData.email, basicRoles)
            ).right().bind()
        }

    override suspend fun signInUser(loginUserBasicData: LoginUserBasicData): Either<LoginException, LoginUserData> =
        either {
            UserDao
                .findUserByEmail(loginUserBasicData.email)
                .toEither { LoginException.USER_NOT_FOUND }
                .bind()

            val user = UserDao
                .tryLogin(loginUserBasicData.email, loginUserBasicData.password)
                .toEither { LoginException.WRONG_PASSWORD }
                .bind()

            val userRoles = UserDao
                .getUserRoles(user.id)

            LoginUserData(
                loginUserDTO = user,
                roles = userRoles,
                jwtToken = tokenCreationService.createToken(loginUserBasicData.email, userRoles)
            ).right().bind()
        }
}