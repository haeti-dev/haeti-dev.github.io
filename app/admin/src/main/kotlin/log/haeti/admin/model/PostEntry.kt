package log.haeti.admin.model

import java.nio.file.Path

private val DATE_PREFIX_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}-(.+)$")

/**
 * 사이드바에 표시되는 글 목록 항목. 프론트매터에서 읽은 값만 담는다.
 *
 * @param dirName 글 디렉터리명 (`yyyy-MM-dd-slug` 형식)
 * @param path 글 디렉터리의 실제 파일 경로
 * @param title 프론트매터 제목. 비어 있으면 빈 문자열
 * @param date 프론트매터 날짜 문자열. 없으면 디렉터리명의 날짜
 * @param category 프론트매터 카테고리. 비어 있으면 빈 문자열
 * @param draft 초안 여부
 * @param translation 영어 번역본(`index.en.md`) 상태
 */
data class PostEntry(
    val dirName: String,
    val path: Path,
    val title: String = "",
    val date: String = dirName.take(10),
    val category: String = "",
    val draft: Boolean = false,
    val translation: TranslationStatus = TranslationStatus.Missing,
) {
    /** 디렉터리명에서 날짜 접두사를 제거한 슬러그. 형식이 어긋나면 [dirName] 전체를 반환한다. */
    val slug: String
        get() = DATE_PREFIX_PATTERN.find(dirName)?.groupValues?.get(1) ?: dirName
}
