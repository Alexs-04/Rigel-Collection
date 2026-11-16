package com.korebit.rigel.dto

import org.springframework.data.domain.Page
import java.io.Serializable

/** Generic page envelope for paginated listings. */
data class PageDto<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) : Serializable {
    companion object {
        fun <E : Any, T> from(page: Page<E>, mapper: (E) -> T): PageDto<T> = PageDto(
            items = page.content.map(mapper),
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}
