package pl.edu.agh.utils

import kotlinx.serialization.Serializable

@Serializable
data class DBQueryResponseWithCount<T : Any>(val data: List<T>, val count: Long)
