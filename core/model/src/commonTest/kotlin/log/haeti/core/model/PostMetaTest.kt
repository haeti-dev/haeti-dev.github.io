package log.haeti.core.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** [PostMeta.directoryName], [dotDate], [readingTimeLabel]이 콘텐츠 규약대로 문자열을 만드는지 확인한다. */
class PostMetaTest {
    @Test
    fun directoryNameJoinsIsoDateAndSlug() {
        val meta = metaOf(LocalDate(2026, 8, 18), "hello-haeti-log")

        assertEquals("2026-08-18-hello-haeti-log", meta.directoryName)
    }

    @Test
    fun directoryNamePadsSingleDigitMonthAndDay() {
        val meta = metaOf(LocalDate(2026, 1, 5), "new-year")

        assertEquals("2026-01-05-new-year", meta.directoryName)
    }

    @Test
    fun dotDateUsesDotSeparators() {
        val meta = metaOf(LocalDate(2026, 8, 18), "hello-haeti-log")

        assertEquals("2026.08.18", meta.dotDate())
    }

    @Test
    fun dotDatePadsSingleDigitMonthAndDay() {
        val meta = metaOf(LocalDate(2026, 1, 5), "new-year")

        assertEquals("2026.01.05", meta.dotDate())
    }

    @Test
    fun readingTimeLabelCapsAtFifteenMinutes() {
        val meta = metaOf(LocalDate(2026, 1, 5), "new-year")

        assertEquals("15 min read", meta.copy(readingMinutes = 15).readingTimeLabel())
        assertEquals("15+ min read", meta.copy(readingMinutes = 31).readingTimeLabel())
    }

    private fun metaOf(
        date: LocalDate,
        slug: String,
    ) = PostMeta(
        title = "제목",
        date = date,
        category = "안드로이드",
        summary = "요약",
        slug = slug,
    )
}
