package log.haeti.feature.home

import log.haeti.core.model.Post

/** 본문 일치 구간 앞뒤로 보여 줄 글자 수. */
private const val EXCERPT_BEFORE = 30
private const val EXCERPT_AFTER = 70

private val IMAGE_LINE = Regex("""!\[[^\]]*]\([^)]*\)(\{[^}]*})?""")
private val LINK = Regex("""\[([^\]]*)]\([^)]*\)""")
private val MARKUP = Regex("""(^|\n)\s*(#{1,6}|>|[-*+]|\d+\.)\s+|```\w*|[*_`\\]|::youtube\[[^\]]*]""")
private val SPACES = Regex("""\s+""")

/**
 * 검색 결과 한 줄.
 *
 * @param post 찾은 글
 * @param excerpt 제목·요약에는 없고 본문에서만 찾았을 때, 일치 구간 주변 문장. 아니면 null
 */
internal data class SearchHit(
    val post: Post,
    val excerpt: String?,
)

/**
 * 검색용으로 미리 다듬어 둔 글 목록. 마크다운 기호 제거는 무거워서, 글 목록이 바뀔 때 한 번만 한다
 * (글자를 칠 때마다 하면 웹은 그만큼 멈춘다).
 *
 * @param posts 발행 글(최신순)
 */
internal class SearchIndex(
    val posts: List<Post>,
) {
    /** 글마다 소문자 제목·요약·카테고리, 기호를 제거한 본문, 그 소문자. */
    val entries: List<Entry> =
        posts.map { post ->
            val body = plainText(post.body)
            val heading = "${post.meta.title} ${post.meta.summary} ${post.meta.category}".lowercase()
            Entry(post, heading, body, body.lowercase())
        }

    /**
     * 색인 한 줄.
     *
     * @param post 글
     * @param heading 소문자 제목·요약·카테고리
     * @param body 기호를 제거한 본문
     * @param lowerBody [body]의 소문자
     */
    class Entry(
        val post: Post,
        val heading: String,
        val body: String,
        val lowerBody: String,
    )
}

/**
 * 제목·요약·카테고리·본문에서 [query]를 찾는다. 띄어쓰기로 나눈 낱말이 모두 들어 있어야 하고, 대소문자는 가리지 않는다.
 * 순서는 색인 그대로(최신순)다.
 *
 * @param index 찾을 글 색인
 * @param query 검색어. 비어 있으면 아무것도 찾지 않는다
 * @param category 이 카테고리 글만 찾는다. null이면 전체
 */
internal fun searchPosts(
    index: SearchIndex,
    query: String,
    category: String? = null,
): List<SearchHit> {
    val terms =
        query
            .trim()
            .lowercase()
            .split(SPACES)
            .filter { it.isNotEmpty() }
    if (terms.isEmpty()) return emptyList()
    return index.entries.mapNotNull { entry ->
        if (category != null && entry.post.meta.category != category) return@mapNotNull null
        if (!terms.all { it in entry.heading || it in entry.lowerBody }) return@mapNotNull null
        val bodyOnly = terms.firstOrNull { it !in entry.heading }
        SearchHit(entry.post, bodyOnly?.let { excerpt(entry.body, entry.lowerBody.indexOf(it), it.length) })
    }
}

/** 마크다운 기호·이미지 줄을 제거한 본문. 검색과 발췌에 쓴다. */
internal fun plainText(markdown: String): String =
    markdown
        .replace(IMAGE_LINE, " ")
        .replace(LINK, "$1")
        .replace(MARKUP, " ")
        .replace(SPACES, " ")
        .trim()

private fun excerpt(
    body: String,
    start: Int,
    length: Int,
): String {
    val from = (start - EXCERPT_BEFORE).coerceAtLeast(0)
    val to = (start + length + EXCERPT_AFTER).coerceAtMost(body.length)
    val prefix = if (from > 0) "…" else ""
    val suffix = if (to < body.length) "…" else ""
    return prefix + body.substring(from, to).trim() + suffix
}
