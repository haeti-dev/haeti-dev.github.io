package log.haeti.admin

import log.haeti.admin.io.ImageFile
import log.haeti.admin.io.MAX_IMAGE_WIDTH
import log.haeti.admin.io.optimizeImage
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageOptimizerTest {
    @Test
    fun 넓은_이미지는_최대_폭으로_줄이고_webp로_바꾼다() {
        val result = optimizeImage(ImageFile("shot", "png", noisyPng(3200, 1000)))

        assertEquals("webp", result.extension)
        assertEquals("shot", result.baseName)
        val decoded = Image.makeFromEncoded(result.bytes)
        assertEquals(MAX_IMAGE_WIDTH, decoded.width)
        assertEquals(500, decoded.height)
    }

    @Test
    fun 큰_png는_webp가_더_작으면_바꾼다() {
        val source = noisyPng(800, 600)

        val result = optimizeImage(ImageFile("photo", "png", source))

        assertEquals("webp", result.extension)
        assertTrue(result.bytes.size < source.size)
    }

    @Test
    fun 줄일_필요_없는_webp는_다시_압축하지_않는다() {
        val webp = solidImage(400, 300).encodeToData(EncodedImageFormat.WEBP, 90)!!.bytes

        val result = optimizeImage(ImageFile("small", "webp", webp))

        assertEquals("webp", result.extension)
        assertContentEquals(webp, result.bytes)
    }

    @Test
    fun gif와_해석할_수_없는_파일은_그대로_둔다() {
        val gif = ImageFile("anim", "gif", byteArrayOf(1, 2, 3))
        val broken = ImageFile("broken", "png", byteArrayOf(1, 2, 3))

        assertEquals(gif, optimizeImage(gif))
        assertEquals(broken, optimizeImage(broken))
    }

    @Test
    fun 세로로_찍은_사진의_EXIF_회전이_webp_픽셀에_남는다() {
        // 저장된 픽셀은 가로(왼쪽 빨강·오른쪽 파랑)이고 EXIF 6(시계 방향 90도)이면 위 빨강·아래 파랑 세로 사진이다.
        val stored =
            encode(3200, 1600, EncodedImageFormat.JPEG) { canvas ->
                canvas.drawRect(Rect.makeXYWH(0f, 0f, 1600f, 1600f), Paint().apply { color = Color.RED })
                canvas.drawRect(Rect.makeXYWH(1600f, 0f, 1600f, 1600f), Paint().apply { color = Color.BLUE })
            }

        val result = optimizeImage(ImageFile("photo", "jpg", withExifOrientation(stored, rotate90 = 6)))

        val pixels = Bitmap.makeFromImage(Image.makeFromEncoded(result.bytes))
        assertEquals(1600, pixels.width)
        assertEquals(3200, pixels.height)
        assertTrue(Color.getR(pixels.getColor(800, 400)) > 200, "위쪽은 빨강")
        assertTrue(Color.getB(pixels.getColor(800, 2800)) > 200, "아래쪽은 파랑")
    }

    /** JPEG SOI 바로 뒤에 Orientation 태그 하나짜리 EXIF(APP1)를 끼운다. */
    private fun withExifOrientation(
        jpeg: ByteArray,
        rotate90: Int,
    ): ByteArray {
        val tiff =
            byteArrayOf(
                0x4D,
                0x4D,
                0,
                0x2A,
                0,
                0,
                0,
                8,
                0,
                1,
                0x01,
                0x12,
                0,
                3,
                0,
                0,
                0,
                1,
                0,
                rotate90.toByte(),
                0,
                0,
            ) +
                byteArrayOf(0, 0, 0, 0)
        val payload = "Exif".toByteArray() + byteArrayOf(0, 0) + tiff
        val length = payload.size + 2
        val app1 = byteArrayOf(0xFF.toByte(), 0xE1.toByte(), (length shr 8).toByte(), length.toByte()) + payload
        return jpeg.copyOfRange(0, 2) + app1 + jpeg.copyOfRange(2, jpeg.size)
    }

    private fun noisyPng(
        width: Int,
        height: Int,
    ): ByteArray =
        encodePng(width, height) { canvas ->
            val random = Random(7)
            val paint = Paint()
            for (y in 0 until height step 8) {
                for (x in 0 until width step 8) {
                    paint.color = Color.makeRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256))
                    canvas.drawRect(Rect.makeXYWH(x.toFloat(), y.toFloat(), 8f, 8f), paint)
                }
            }
        }

    private fun solidImage(
        width: Int,
        height: Int,
    ): Image = Surface.makeRasterN32Premul(width, height).apply { canvas.clear(Color.WHITE) }.makeImageSnapshot()

    private fun encodePng(
        width: Int,
        height: Int,
        draw: (org.jetbrains.skia.Canvas) -> Unit,
    ): ByteArray = encode(width, height, EncodedImageFormat.PNG, draw)

    private fun encode(
        width: Int,
        height: Int,
        format: EncodedImageFormat,
        draw: (org.jetbrains.skia.Canvas) -> Unit,
    ): ByteArray {
        val surface = Surface.makeRasterN32Premul(width, height)
        draw(surface.canvas)
        return surface.makeImageSnapshot().encodeToData(format)!!.bytes
    }
}
