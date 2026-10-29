package log.haeti.core.data.parser

import kotlinx.datetime.LocalDate
import log.haeti.core.model.Language
import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta
import kotlin.math.ceil

/**
 * 마크다운 원문(프론트매터 + 본문)을 파싱해 [Post]로 변환한다.
 */
object PostParser {
    private val requiredKeys = listOf("title", "date", "category", "summary")

    /** 한글을 읽는 속도(음절/분). 기술 글은 훑어 읽으므로 Medium 기준(500)보다 넉넉히 잡는다. */
    private const val SYLLABLES_PER_MINUTE = 700

    /** 영어 단어·코드 토큰을 읽는 속도(단어/분). 같은 이유로 Medium 기준(265)보다 넉넉히 잡는다. */
    private const val WORDS_PER_MINUTE = 300

    /** 독자가 읽지 않는 부분: 이미지 줄 전체, 링크의 주소, 본문에 그대로 쓴 URL. */
    private val unreadMarkup = Regex("""!\[[^\]]*]\([^)]*\)(\{[^}]*})?|]\([^)]*\)|https?://\S+""")
    private val hangulSyllable = Regex("[가-힣]")
    private val latinWord = Regex("[A-Za-z0-9_]+")

    /**
     * raw 마크다운 전문에서 프론트매터를 파싱해 [Post]로 만든다.
     * slug는 파싱하지 않고 파라미터로 받은 디렉터리명을 그대로 사용한다.
     *
     * @param raw `---` 펜스로 감싼 프론트매터와 마크다운 본문 전체
     * @param slug 디렉터리명에서 온 URL 슬러그
     * @return 파싱된 [Post]
     * @throws IllegalArgumentException 펜스가 없거나 필수 키가 누락됐거나 date 형식이 잘못된 경우
     */
    fun parse(
        raw: String,
        slug: String,
    ): Post {
        val (fields, body) = splitFrontmatter(raw)
        val missing = requiredKeys.filter { it !in fields }
        require(missing.isEmpty()) {
            "프론트매터에 필수 키가 없습니다: ${missing.joinToString(", ")}"
        }
        val meta =
            PostMeta(
                title = fields.getValue("title"),
                date = parseDate(fields.getValue("date")),
                category = fields.getValue("category"),
                summary = fields.getValue("summary"),
                slug = slug,
                thumbnail = fields["thumbnail"],
                draft = fields["draft"]?.toBooleanStrictOrNull() ?: false,
                readingMinutes = readingMinutesOf(body),
            )
        return Post(meta = meta, body = body)
    }

    /**
     * 번역본(`index.en.md`)을 원문 [base] 위에 덮어 [Post]로 만든다.
     * 번역본 프론트매터에는 `title`·`summary`만 있으면 되고, 날짜·카테고리·썸네일은 원문을 따른다.
     *
     * @param raw 번역본 원문 (프론트매터 + 본문)
     * @param base 같은 글의 한국어 원문
     * @param language 번역본 언어
     */
    fun parseTranslation(
        raw: String,
        base: Post,
        language: Language,
    ): Post {
        val (fields, body) = splitFrontmatter(raw)
        val meta =
            base.meta.copy(
                title = fields["title"] ?: base.meta.title,
                summary = fields["summary"] ?: base.meta.summary,
                readingMinutes = readingMinutesOf(body),
            )
        return Post(meta = meta, body = body, language = language)
    }

    /**
     * 읽기 분량을 어림한다. 한글은 음절 700개, 영어·코드는 단어 300개를 1분으로 치고 올림한다. 최소 1분.
     * 이미지 경로·링크 주소·마크다운 기호처럼 화면에서 읽지 않는 글자는 세지 않는다.
     */
    fun readingMinutesOf(body: String): Int {
        val text = unreadMarkup.replace(body, "]")
        val syllables = hangulSyllable.findAll(text).count()
        val words = latinWord.findAll(text).count()
        val minutes = syllables.toDouble() / SYLLABLES_PER_MINUTE + words.toDouble() / WORDS_PER_MINUTE
        return ceil(minutes).toInt().coerceAtLeast(1)
    }

    /** `yyyy-MM-dd` 날짜를 파싱한다. 형식이 어긋나면 원본 값을 메시지에 담아 실패한다. */
    private fun parseDate(value: String): LocalDate =
        try {
            LocalDate.parse(value)
        } catch (cause: IllegalArgumentException) {
            throw IllegalArgumentException("date 형식이 yyyy-MM-dd가 아닙니다: $value", cause)
        }

    /** `---` 펜스 사이의 `key: value` 라인을 파싱해 필드 맵과 본문으로 분리한다. */
    private fun splitFrontmatter(raw: String): Pair<Map<String, String>, String> {
        val lines = raw.lines()
        require(lines.firstOrNull() == "---") {
            "프론트매터가 '---'로 시작하지 않습니다"
        }
        val closingIndex = lines.drop(1).indexOf("---") + 1
        require(closingIndex > 0) {
            "프론트매터를 닫는 '---'를 찾을 수 없습니다"
        }
        val fields = lines.subList(1, closingIndex).mapNotNull(::parseLine).toMap()
        val body = lines.drop(closingIndex + 1).joinToString("\n").trimStart('\n')
        return fields to body
    }

    /** `key: value` 한 줄을 파싱한다. 빈 줄은 무시(null)한다. */
    private fun parseLine(line: String): Pair<String, String>? {
        if (line.isBlank()) return null
        val separatorIndex = line.indexOf(':')
        require(separatorIndex > 0) { "프론트매터 라인 형식이 잘못됐습니다: $line" }
        val key = line.substring(0, separatorIndex).trim()
        val value = line.substring(separatorIndex + 1).trim()
        return key to value
    }
}
