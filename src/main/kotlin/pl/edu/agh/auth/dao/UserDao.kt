package pl.edu.agh.auth.dao

import arrow.core.Option
import arrow.core.flattenOption
import arrow.core.singleOrNone
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserDTO
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.table.RoleTable
import pl.edu.agh.auth.table.UserRolesTable
import pl.edu.agh.auth.table.UserTable

object UserDao {

    fun tryLogin(email: String, password: String): Option<LoginUserDTO> =
        UserTable
            .select { UserTable.email eq email and (UserTable.password eq password) }
            .singleOrNone()
            .map { UserTable.toDomain(it) }


    fun getUserRoles(userId: LoginUserId): List<Roles> =
        RoleTable
            .join(UserRolesTable, JoinType.INNER, additionalConstraint = {
                UserRolesTable.roleId eq RoleTable.roleId
            })
            .select { UserRolesTable.userId eq userId }
            .map { Roles.fromId(it[RoleTable.roleId]) }
            .flattenOption()
}