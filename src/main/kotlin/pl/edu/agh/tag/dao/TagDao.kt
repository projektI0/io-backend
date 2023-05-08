package pl.edu.agh.tag.dao

import org.jetbrains.exposed.sql.selectAll
import pl.edu.agh.tag.domain.dto.TagTableDTO
import pl.edu.agh.tag.table.TagTable

object TagDao {
    fun getAllTags() : List<TagTableDTO> =
        TagTable
        .selectAll()
        .map { TagTable.toDomain(it) }
}