package log.haeti.admin.io

/**
 * 원문 맨 앞 `---` 펜스 사이의 `key: value` 줄을 느슨하게 파싱한다.
 * 필수 키 검사·예외 없이 있는 값만 돌려준다. 쓰다 만 글도 목록에 보여야 하기 때문이다.
 *
 * @param text 마크다운 원문 전체
 */
internal fun peekFrontmatter(text: String): Map<String, String> {
    val lines = text.lines()
    if (lines.firstOrNull()?.trim() != "---") return emptyMap()
    return lines
        .drop(1)
        .takeWhile { it.trim() != "---" }
        .mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) return@mapNotNull null
            line.substring(0, separator).trim() to line.substring(separator + 1).trim()
        }.toMap()
}
