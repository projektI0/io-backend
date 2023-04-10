package pl.edu.agh.tag.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import pl.edu.agh.tag.domain.TagId
import pl.edu.agh.tag.domain.tagId

object TagTable : Table("TAG") {
    val id: Column<TagId> = tagId("ID").autoIncrement()
    val name: Column<String> = varchar("NAME", 256)
    val parentTagId: Column<TagId?> = tagId("PARENT_TAG_ID").nullable()
}
