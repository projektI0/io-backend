package pl.edu.agh.auth.service

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.batchInsert
import pl.edu.agh.auth.dao.UserDao
import pl.edu.agh.auth.domain.LoginUserResponse
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.domain.dto.LoginUserDTO
import pl.edu.agh.auth.domain.request.LoginUserRequest
import pl.edu.agh.auth.table.UserRolesTable
import pl.edu.agh.utils.DomainException

sealed class RegisterException(userMessage: String, internalMessage: String) : DomainException(
    HttpStatusCode.BadRequest,
    userMessage,
    internalMessage
) {
    class EmailAlreadyExists(email: String) :
        RegisterException("Email already exists", "Email already exists while registering user with email: $email")

    object PasswordTooShort : RegisterException("Password is too short", "Password is too short while registering user")
}

sealed class LoginException(userMessage: String, internalMessage: String) : DomainException(
    HttpStatusCode.BadRequest,
    userMessage,
    internalMessage
) {
    class UserNotFound(email: String) :
        LoginException("Wrong login or password", "User not found while logging in user with email: $email")

    class WrongPassword(email: String) :
        LoginException("Wrong login or password", "Wrong password while logging in user with email: $email")
}

interface AuthService {
    suspend fun signUpNewUser(loginUserRequest: LoginUserRequest): Either<RegisterException, LoginUserResponse>
    suspend fun signInUser(loginUserRequest: LoginUserRequest): Either<LoginException, LoginUserResponse>
}

class AuthServiceImpl(private val tokenCreationService: TokenCreationService) : AuthService {

    override suspend fun signUpNewUser(loginUserRequest: LoginUserRequest): Either<RegisterException, LoginUserResponse> =
        either {
            Either.conditionally(
                loginUserRequest.password.length > 8,
                ifFalse = { RegisterException.PasswordTooShort },
                ifTrue = { }
            ).bind()

            UserDao.findUserByEmail(loginUserRequest.email)
                .map { RegisterException.EmailAlreadyExists(loginUserRequest.email) }
                .toEither { }.swap().bind()

            val newId = UserDao.insertNewUser(loginUserRequest)
            val basicRoles = listOf(Roles.USER)

            UserRolesTable.batchInsert(basicRoles) { role ->
                this[UserRolesTable.roleId] = role.id
                this[UserRolesTable.userId] = newId
            }

            LoginUserResponse(
                loginUserDTO = LoginUserDTO(id = newId, email = loginUserRequest.email),
                roles = basicRoles,
                jwtToken = tokenCreationService.createToken(loginUserRequest.email, basicRoles, newId)
            ).right().bind()
        }

    override suspend fun signInUser(loginUserRequest: LoginUserRequest): Either<LoginException, LoginUserResponse> =
        either {
            UserDao.findUserByEmail(loginUserRequest.email)
                .toEither { LoginException.UserNotFound(loginUserRequest.email) }.bind()

            val user = UserDao.tryLogin(loginUserRequest.email, loginUserRequest.password)
                .toEither { LoginException.WrongPassword(loginUserRequest.email) }.bind()

            val userRoles = UserDao.getUserRoles(user.id)

            LoginUserResponse(
                loginUserDTO = user,
                roles = userRoles,
                jwtToken = tokenCreationService.createToken(loginUserRequest.email, userRoles, user.id)
            ).right().bind()
        }
}
