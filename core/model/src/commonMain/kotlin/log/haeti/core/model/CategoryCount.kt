package log.haeti.core.model

import androidx.compose.runtime.Immutable

/**
 * 카테고리 하나와 거기 속한 발행 글 수.
 *
 * @param name 카테고리 표시명
 * @param count 글 수
 */
@Immutable
data class CategoryCount(
    val name: String,
    val count: Int,
)
