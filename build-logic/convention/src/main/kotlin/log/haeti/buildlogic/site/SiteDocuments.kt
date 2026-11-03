package log.haeti.buildlogic.site

import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** index.html 안에서 페이지별 메타 태그가 들어갈 자리. */
internal const val HEAD_MARKER = "<!-- haeti:head -->"

private val TITLE_TAG = Regex("<title>.*?</title>")
private val SEOUL = ZoneId.of("Asia/Seoul")
private const val FEED_SIZE = 20

/**
 * 사이트 전역 정보.
 *
 * @param url 끝 `/` 없는 절대 주소 (`https://haeti-dev.github.io/haeti-log`)
 * @param name 사이트 이름
 * @param description 사이트 한 줄 소개
 */
internal data class SiteInfo(
    val url: String,
    val name: String,
    val description: String,
)

/**
 * 셸 한 장의 메타 정보.
 *
 * @param path 사이트 루트 기준 경로 (`/`, `/post/<slug>/`)
 * @param title 문서·OG 제목
 * @param description 요약
 * @param image 사이트 루트 기준 OG 이미지 경로
 * @param post 글 셸이면 그 글. 홈·고정 페이지면 null
 * @param isStatic 소개·프로젝트 같은 고정 페이지인지 (문서 제목에 사이트 이름을 붙인다)
 */
internal data class ShellPage(
    val path: String,
    val title: String,
    val description: String,
    val image: String,
    val post: SitePost? = null,
    val isStatic: Boolean = false,
)

/** [template](배포용 index.html)에 [page]의 제목·설명·OG·트위터 카드·RSS 링크를 채운다. */
internal fun renderShell(
    template: String,
    page: ShellPage,
    site: SiteInfo,
): String {
    check(HEAD_MARKER in template) { "index.html에 $HEAD_MARKER 자리가 없습니다" }
    val url = site.url + page.path
    val tags =
        buildList {
            add("""<meta name="description" content="${page.description.escapeXml()}" />""")
            add("""<link rel="canonical" href="$url" />""")
            val feed = "${site.url}/feed.xml"
            add("""<link rel="alternate" type="application/rss+xml" title="${site.name}" href="$feed" />""")
            add("""<meta property="og:site_name" content="${site.name}" />""")
            add("""<meta property="og:type" content="${if (page.post == null) "website" else "article"}" />""")
            add("""<meta property="og:title" content="${page.title.escapeXml()}" />""")
            add("""<meta property="og:description" content="${page.description.escapeXml()}" />""")
            add("""<meta property="og:url" content="$url" />""")
            add("""<meta property="og:image" content="${site.url}${page.image}" />""")
            add("""<meta property="og:locale" content="ko_KR" />""")
            page.post?.let { add("""<meta property="article:published_time" content="${it.date}" />""") }
            add("""<meta name="twitter:card" content="summary_large_image" />""")
        }
    val title = if (page.post == null && !page.isStatic) site.name else "${page.title} | ${site.name}"
    return template
        // 람다로 넘겨야 제목의 `$`·`\`를 정규식 치환 기호로 읽지 않는다(`$this` 같은 제목에서 빌드가 깨졌다).
        .replace(TITLE_TAG) { "<title>${title.escapeXml()}</title>" }
        .replace(HEAD_MARKER, tags.joinToString("\n    "))
}

/** 검색엔진용 페이지 목록. 홈·고정 페이지·발행 글을 담는다. */
internal fun renderSitemap(
    site: SiteInfo,
    staticPaths: List<String>,
    posts: List<SitePost>,
): String {
    val entries =
        (listOf("/" to posts.firstOrNull()?.date) + staticPaths.map { it to null } + posts.map { it.path to it.date })
            .joinToString("\n") { (path, date) ->
                val lastmod = date?.let { "<lastmod>$it</lastmod>" }.orEmpty()
                "  <url><loc>${site.url}$path</loc>$lastmod</url>"
            }
    return """
        |<?xml version="1.0" encoding="UTF-8"?>
        |<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
        |$entries
        |</urlset>
        |
    """.trimMargin()
}

/** 구독 앱용 RSS 2.0. 최신 [FEED_SIZE]편의 제목·요약·링크·날짜를 담는다. */
internal fun renderFeed(
    site: SiteInfo,
    posts: List<SitePost>,
): String {
    val items =
        posts.take(FEED_SIZE).joinToString("\n") { post ->
            val link = site.url + post.path
            """
            |    <item>
            |      <title>${post.title.escapeXml()}</title>
            |      <link>$link</link>
            |      <guid isPermaLink="true">$link</guid>
            |      <pubDate>${rfc822(post)}</pubDate>
            |      <category>${post.category.escapeXml()}</category>
            |      <description>${post.summary.escapeXml()}</description>
            |    </item>
            """.trimMargin()
        }
    return """
        |<?xml version="1.0" encoding="UTF-8"?>
        |<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
        |  <channel>
        |    <title>${site.name}</title>
        |    <link>${site.url}/</link>
        |    <description>${site.description.escapeXml()}</description>
        |    <language>ko</language>
        |    <atom:link href="${site.url}/feed.xml" rel="self" type="application/rss+xml" />
        |$items
        |  </channel>
        |</rss>
        |
    """.trimMargin()
}

/** 크롤러 허용과 사이트맵 위치. 도메인 루트에 있을 때만 크롤러가 읽는다(커스텀 도메인·`<user>.github.io`). */
internal fun renderRobots(site: SiteInfo): String = "User-agent: *\nAllow: /\n\nSitemap: ${site.url}/sitemap.xml\n"

private fun rfc822(post: SitePost): String =
    post.date.atStartOfDay(SEOUL).format(DateTimeFormatter.RFC_1123_DATE_TIME)

/** XML·HTML 속성과 본문에 안전한 문자열. */
internal fun String.escapeXml(): String =
    replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
