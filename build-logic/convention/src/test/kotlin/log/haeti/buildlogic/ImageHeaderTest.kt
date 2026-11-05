package log.haeti.buildlogic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 이미지 파일 머리에서 크기를 읽는 규칙을 확인한다. */
class ImageHeaderTest {
    @Test
    fun PNG_GIF_JPEG_크기를_읽는다() {
        // 한 바이트(255)를 넘는 값이어야 바이트를 합치는 순서까지 확인된다.
        for (format in listOf("png", "gif", "jpg")) {
            assertEquals(ImageSize(300, 200), readImageSize(encodeImage(format, 300, 200)), format)
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
        val header = webpHeader("VP8L") + byteArrayOf(0x2f) + littleEndian(bits.toLong(), 4)

        assertEquals(ImageSize(300, 200), readImageSize(header))
    }

    @Test
    fun 확장_WebP_크기를_읽는다() {
        val header = webpHeader("VP8X") + ByteArray(4) + littleEndian(1919, 3) + littleEndian(1079, 3)

        assertEquals(ImageSize(1920, 1080), readImageSize(header))
    }

    @Test
    fun 프로그레시브_JPEG는_허프만_표를_건너뛰고_프레임_헤더에서_읽는다() {
        // SOI, 허프만 표(C4: 프레임 헤더가 아님), 프로그레시브 프레임 헤더(C2): 정밀도 8, 높이 200, 너비 300
        val huffman = byteArrayOf(0xFF.toByte(), 0xC4.toByte(), 0x00, 0x04, 0x00, 0x00)
        val frame = byteArrayOf(0xFF.toByte(), 0xC2.toByte(), 0x00, 0x0B, 0x08, 0x00, 0xC8.toByte(), 0x01, 0x2C) +
            ByteArray(6)
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte()) + huffman + frame

        assertEquals(ImageSize(300, 200), readImageSize(bytes))
    }

    @Test
    fun 모르는_형식은_null이다() {
        assertNull(readImageSize("not an image".toByteArray()))
    }

    @Test
    fun 크기가_있을_자리까지_잘린_파일은_null이다() {
        val truncated =
            mapOf(
                "png" to encodeImage("png", 300, 200).copyOf(20),
                "gif" to encodeImage("gif", 300, 200).copyOf(8),
                "VP8 " to webpHeader("VP8 ") + ByteArray(6),
                "VP8L" to webpHeader("VP8L") + ByteArray(2),
                "VP8X" to webpHeader("VP8X") + ByteArray(6),
            )

        truncated.forEach { (name, bytes) -> assertNull(readImageSize(bytes), name) }
    }

    @Test
    fun 프레임_헤더가_없는_JPEG는_null이다() {
        val jpeg = encodeImage("jpg", 300, 200)
        val frameAt = (2 until jpeg.size - 1).first { jpeg[it] == 0xFF.toByte() && jpeg[it + 1] == 0xC0.toByte() }
        val broken =
            mapOf(
                "프레임 헤더 앞에서 잘림" to jpeg.copyOf(frameAt),
                "세그먼트 대신 쓰레기 바이트" to byteArrayOf(0xFF.toByte(), 0xD8.toByte()) + ByteArray(20) { 0x41 },
            )

        broken.forEach { (name, bytes) -> assertNull(readImageSize(bytes), name) }
    }
}
