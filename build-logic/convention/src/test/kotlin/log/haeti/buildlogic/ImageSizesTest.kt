package log.haeti.buildlogic

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/** 배포용 글에 이미지 크기를 기록하는 규칙을 확인한다. 파일 머리 읽기는 [ImageHeaderTest]가 맡는다. */
class ImageSizesTest {
    private val site = Files.createTempDirectory("image-sizes").toFile()
    private val post = site.resolve("post").apply { mkdirs() }

    @Test
    fun 지시어가_없으면_크기만_담은_지시어를_만든다() {
        image("images/a.png", 40, 30)

        assertEquals("![그림](images/a.png){size=40x30}", withImageSizes("![그림](images/a.png)", post))
    }

    @Test
    fun 지시어가_있으면_끝에_덧붙인다() {
        image("images/a.png", 40, 30)

        assertEquals(
            """![](images/a.png){width=60 caption="설명" size=40x30}""",
            withImageSizes("""![](images/a.png){width=60 caption="설명"}""", post),
        )
    }

    @Test
    fun 없는_파일과_이미_기록된_크기는_그대로_둔다() {
        image("images/a.png", 40, 30)
        val unchanged = listOf("![](images/none.png)", "![](images/a.png){size=1x1}")

        unchanged.forEach { assertEquals(it, withImageSizes(it, post)) }
    }

    @Test
    fun 글_디렉터리_밖에_실제로_있는_파일도_읽지_않는다() {
        site.resolve("secret.png").writeBytes(encodeImage("png", 40, 30))

        assertEquals("![](../secret.png)", withImageSizes("![](../secret.png)", post))
    }

    @Test
    fun 코드_블록_안_예시는_그대로_둔다() {
        image("images/a.png", 40, 30)
        val markdown = "```markdown\n![](images/a.png)\n```\n![](images/a.png)"

        assertEquals("```markdown\n![](images/a.png)\n```\n![](images/a.png){size=40x30}", withImageSizes(markdown, post))
    }

    @Test
    fun 긴_펜스_안의_짧은_펜스는_블록을_닫지_않는다() {
        image("images/a.png", 40, 30)
        val markdown = "````markdown\n```\n![](images/a.png)\n````\n![](images/a.png)"

        assertEquals("````markdown\n```\n![](images/a.png)\n````\n![](images/a.png){size=40x30}", withImageSizes(markdown, post))
    }

    @Test
    fun 다른_문자의_펜스와_뒤에_글자가_붙은_펜스는_블록을_닫지_않는다() {
        image("images/a.png", 40, 30)

        for (inner in listOf("~~~", "```kotlin")) {
            val markdown = "```\n$inner\n![](images/a.png)\n```"
            assertEquals(markdown, withImageSizes(markdown, post), inner)
        }
    }

    @Test
    fun 다크_이미지_비율이_다르면_크기를_적지_않는다() {
        image("images/a.png", 40, 30)
        image("images/a-dark.png", 40, 20)
        image("images/b-dark.png", 80, 60)

        assertEquals("![](images/a.png){dark=images/a-dark.png}", withImageSizes("![](images/a.png){dark=images/a-dark.png}", post))
        assertEquals(
            "![](images/a.png){dark=images/b-dark.png size=40x30}",
            withImageSizes("![](images/a.png){dark=images/b-dark.png}", post),
        )
    }

    @Test
    fun 다크_이미지_경로가_틀리면_빌드를_멈추지_않고_크기를_적지_않는다() {
        image("images/a.png", 40, 30)
        val markdown = "![](images/a.png){dark=images/typo.png}"

        assertEquals(markdown, withImageSizes(markdown, post))
    }

    @Test
    fun 지시어가_있는_경로를_다시_쓴_곳에는_덧붙이지_않는다() {
        image("images/a.png", 40, 30)
        val markdown = "![](images/a.png){width=60 caption=\"설명\"}\n\n![](images/a.png)"

        assertEquals(
            "![](images/a.png){width=60 caption=\"설명\" size=40x30}\n\n![](images/a.png)",
            withImageSizes(markdown, post),
        )
    }

    @Test
    fun 캡션_글자_속_size와_dark는_속성으로_보지_않는다() {
        image("images/a.png", 40, 30)
        val markdown = "![](images/a.png){caption=\"size=1x1 dark=images/x.png 예시\"}"

        assertEquals(
            "![](images/a.png){caption=\"size=1x1 dark=images/x.png 예시\" size=40x30}",
            withImageSizes(markdown, post),
        )
    }

    private fun image(
        path: String,
        width: Int,
        height: Int,
    ): File =
        post.resolve(path).apply {
            parentFile.mkdirs()
            writeBytes(encodeImage("png", width, height))
        }
}
