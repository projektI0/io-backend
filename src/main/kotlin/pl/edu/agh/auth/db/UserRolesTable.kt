package pl.edu.agh.auth.db

import arrow.core.flattenOption
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import pl.edu.agh.auth.domain.LoginUserId
import pl.edu.agh.auth.domain.Roles
import pl.edu.agh.auth.domain.loginUserId

object UserRolesTable {
    object RoleTable : Table("ROLE") {
        val roleId: Column<Int> = integer("ID")
        val name: Column<String> = varchar("NAME", 256)
        override val primaryKey = PrimaryKey(roleId)
    }

    object UserRolesTable : Table("LOGIN_USER_ROLE") {
        val userId: Column<LoginUserId> = loginUserId("LOGIN_USER_ID")
        val roleId: Column<Int> = integer("ROLE_ID")
    }

    fun getUserRoles(userId: LoginUserId): List<Roles> =
        RoleTable.join(UserRolesTable, JoinType.INNER, additionalConstraint = {
            UserRolesTable.roleId eq RoleTable.roleId
        }).select { UserRolesTable.userId eq userId }.map { Roles.fromId(it[RoleTable.roleId]) }.flattenOption()
}
