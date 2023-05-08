package pl.edu.agh.tag.domain.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.tag.domain.TagId

@Serializable
data class TagTableDTO(val id: TagId, val name: String, val parentTagId: TagId?)
