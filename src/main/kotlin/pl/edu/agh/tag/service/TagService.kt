package pl.edu.agh.tag.service

import pl.edu.agh.tag.dao.TagDao
import pl.edu.agh.tag.domain.dto.TagTableDTO
import pl.edu.agh.utils.Transactor

interface TagService {
    suspend fun getAllTags(): List<TagTableDTO>
}

class TagServiceImpl : TagService {
    override suspend fun getAllTags(): List<TagTableDTO> = Transactor.dbQuery { TagDao.getAllTags() }
}
