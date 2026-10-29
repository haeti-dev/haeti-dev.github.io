package log.haeti.core.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

/**
 * 글 메타데이터. content-format.md의 프론트매터 스키마와 1:1.
 *
 * @param title 글 제목
 * @param date 발행일
 * @param category 카테고리 표시명
 * @param summary 목록·OG용 한 줄 요약
 * @param slug URL 슬러그
 * @param thumbnail 썸네일 상대경로 (없으면 null)
 * @param draft 초안 여부
 * @param readingMinutes 본문을 읽는 데 걸리는 대략적인 분. 최소 1
 */
@Immutable
data class PostMeta(
    val title: String,
    val date: LocalDate,
    val category: String,
    val summary: String,
    val slug: String,
    val thumbnail: String? = null,
    val draft: Boolean = false,
    val readingMinutes: Int = 1,
) {
    /**
     * 글 디렉터리명 `yyyy-MM-dd-slug`.
     *
     * `content/posts` 아래 실제 디렉터리명이자 번들 리소스 경로의 한 조각이다.
     */
    val directoryName: String get() = "$date-$slug"
}
