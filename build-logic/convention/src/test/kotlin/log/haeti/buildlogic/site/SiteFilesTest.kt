package log.haeti.buildlogic.site

import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SiteFilesTest {
    private val site = SiteInfo("https://example.github.io/blog", "Haeti.log", "소개 & <설명>")
    private val root = Files.createTempDirectory("site-test").toFile()

    @Test
    fun 초안은_빼고_최신순으로_읽는다() {
        post("2026-01-01-old", "title: 옛 글\ndate: 2026-01-01\ncategory: 회고\nsummary: 요약")
        post("2026-02-01-new", "title: 새 글\ndate: 2026-02-01\ncategory: 웹\nsummary: 요약\nthumbnail: images/a.webp")
        post("2026-03-01-draft", "title: 초안\ndate: 2026-03-01\ncategory: 웹\ndraft: true")

        val posts = publishedPosts(root)

        assertEquals(listOf("new", "old"), posts.map { it.slug })
        assertEquals("images/a.webp", posts.first().thumbnail)
        assertEquals("/post/new/", posts.first().path)
    }

    @Test
    fun 셸에_제목과_OG_태그를_채우고_특수문자를_이스케이프한다() {
        val template = "<head><title>Haeti.log</title>\n$HEAD_MARKER\n</head>"
        val post = samplePost("a&b", "\"따옴표\" 제목")

        val html = renderShell(template, ShellPage(post.path, post.title, "<요약>", "/og/a.png", post), site)

        assertTrue("<title>&quot;따옴표&quot; 제목 | Haeti.log</title>" in html)
        assertTrue("""<meta property="og:image" content="https://example.github.io/blog/og/a.png" />""" in html)
        assertTrue("""content="&lt;요약&gt;"""" in html)
        assertTrue("og:type\" content=\"article\"" in html)
        assertFalse(HEAD_MARKER in html)
    }

    @Test
    fun 제목에_달러와_백슬래시가_있어도_셸을_만든다() {
        val template = "<head><title>Haeti.log</title>\n$HEAD_MARKER\n</head>"
        val post = samplePost("dollar", "\$this와 \\n, \$1")

        val html = renderShell(template, ShellPage(post.path, post.title, "요약", "/og/a.png", post), site)

        assertTrue("<title>\$this와 \\n, \$1 | Haeti.log</title>" in html)
    }

    @Test
    fun RSS와_사이트맵은_올바른_XML이다() {
        val posts = listOf(samplePost("x", "R&D <노트>"))

        parse(renderFeed(site, posts))
        parse(renderSitemap(site, listOf("/about/"), posts))
        assertTrue("<pubDate>Wed, 1 Jul 2026 00:00:00 +0900</pubDate>" in renderFeed(site, posts))
    }

    @Test
    fun webp_썸네일을_1200px_JPG로_바꾼다() {
        val source = File(javaClass.getResource("/thumb.webp")!!.toURI())
        val output = root.resolve("og.jpg")

        writeThumbnailOg(source, output)

        val image = ImageIO.read(output)
        assertEquals(OG_WIDTH, image.width)
        assertEquals(600, image.height)
        assertTrue((image.getRGB(100, 300) shr 16 and 0xFF) > 200, "왼쪽은 빨강")
    }

    @Test
    fun 커버는_카테고리_색_바탕의_1200x630_PNG다() {
        val output = root.resolve("cover.png")

        writeCoverOg(samplePost("hello", "제목"), output)

        val image = ImageIO.read(output)
        assertEquals(OG_WIDTH to OG_HEIGHT, image.width to image.height)
        assertEquals(0xE3F4EC, image.getRGB(5, 5) and 0xFFFFFF)
    }

    private fun post(
        name: String,
        frontmatter: String,
    ) = root.resolve(name).apply { mkdirs() }.resolve("index.md").writeText("---\n$frontmatter\n---\n\n본문")

    private fun samplePost(
        slug: String,
        title: String,
    ) = SitePost(root, slug, title, "요약", "안드로이드", java.time.LocalDate.of(2026, 7, 1), null)

    private fun parse(xml: String) =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.byteInputStream())
}
