package pl.edu.agh.tag.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.tag.domain.tagId

object TagTable : IdTable<TagId>("TAG") {
    override val id: Column<EntityID<TagId>> = tagId("ID").autoIncrement().entityId()
    val name: Column<String> = varchar("NAME", 256)
    val parentTagId: Column<TagId?> = tagId("PARENT_TAG_ID").nullable()
}