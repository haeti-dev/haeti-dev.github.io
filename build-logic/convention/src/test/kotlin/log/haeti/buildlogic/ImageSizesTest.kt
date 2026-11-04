package log.haeti.buildlogic

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 배포용 글에 이미지 크기를 기록하는 규칙을 확인한다. */
class ImageSizesTest {
    private val post = Files.createTempDirectory("image-sizes").toFile()

    @Test
    fun PNG_GIF_JPEG_크기를_읽는다() {
        for (format in listOf("png", "gif", "jpg")) {
            assertEquals(ImageSize(3, 5), readImageSize(encode(format, 3, 5)), format)
        }
    }

    @Test
    fun 손실_WebP_크기를_읽는다() {
        val bytes = javaClass.getResourceAsStream("/thumb.webp")!!.readBytes()

        assertEquals(ImageSize(1600, 800), readImageSize(bytes))
    }

    @Test
    fun 무손실_WebP_크기를_읽는다() {
        val bits = 299 or (199 shl 14)
        val header = webp("VP8L") + byteArrayOf(0x2f) + le(bits.toLong(), 4)

        assertEquals(ImageSize(300, 200), readImageSize(header))
    }

    @Test
    fun 확장_WebP_크기를_읽는다() {
        val header = webp("VP8X") + ByteArray(4) + le(1919, 3) + le(1079, 3)

        assertEquals(ImageSize(1920, 1080), readImageSize(header))
    }

    @Test
    fun 모르는_형식은_null이다() {
        assertNull(readImageSize("not an image".toByteArray()))
    }

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
    fun 없는_파일_글_밖_파일_이미_기록된_크기는_그대로_둔다() {
        image("images/a.png", 40, 30)
        val unchanged = listOf("![](images/none.png)", "![](../secret.png)", "![](images/a.png){size=1x1}")

        unchanged.forEach { assertEquals(it, withImageSizes(it, post)) }
    }

    @Test
    fun 코드_블록_안_예시는_그대로_둔다() {
        image("images/a.png", 40, 30)
        val markdown = "```markdown\n![](images/a.png)\n```\n![](images/a.png)"

        assertEquals("```markdown\n![](images/a.png)\n```\n![](images/a.png){size=40x30}", withImageSizes(markdown, post))
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

    private fun encode(
        format: String,
        width: Int,
        height: Int,
    ): ByteArray =
        ByteArrayOutputStream().also {
            ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, it)
        }.toByteArray()

    private fun image(
        path: String,
        width: Int,
        height: Int,
    ): File =
        post.resolve(path).apply {
            parentFile.mkdirs()
            writeBytes(encode("png", width, height))
        }

    private fun webp(chunk: String): ByteArray = "RIFF".toByteArray() + ByteArray(4) + "WEBP${chunk}".toByteArray() + ByteArray(4)

    private fun le(
        value: Long,
        size: Int,
    ): ByteArray = ByteArray(size) { ((value shr (8 * it)) and 0xff).toByte() }
}
