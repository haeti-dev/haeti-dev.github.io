package log.haeti.core.model

/**
 * 화면 전체에 쓰는 `2026.08.18` 점 표기 날짜.
 *
 * [kotlinx.datetime.LocalDate.toString]이 ISO-8601 `yyyy-MM-dd`를 보장하므로 구분자만 바꾼다.
 */
fun PostMeta.dotDate(): String = date.toString().replace('-', '.')

/** 이보다 긴 글은 정확한 분 대신 `15+ min read`로 적는다. 긴 숫자는 읽기 전에 겁부터 준다. */
private const val MAX_SHOWN_MINUTES = 15

/** `6 min read` 형태의 읽기 분량 표기 (Medium 등에서 익숙한 형식). 15분을 넘으면 `15+ min read`. */
fun PostMeta.readingTimeLabel(): String = readingTimeLabel(readingMinutes)

/**
 * 읽기 분량 표기. 블로그와 어드민 상태 줄이 같은 규칙을 쓴다.
 *
 * @param minutes 어림한 읽기 시간(분)
 */
fun readingTimeLabel(minutes: Int): String =
    if (minutes > MAX_SHOWN_MINUTES) "$MAX_SHOWN_MINUTES+ min read" else "$minutes min read"
