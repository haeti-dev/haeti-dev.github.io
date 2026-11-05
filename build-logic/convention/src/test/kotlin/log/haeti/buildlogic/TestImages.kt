package log.haeti.buildlogic

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/** [format](png·gif·jpg)으로 인코딩한 빈 이미지. */
internal fun encodeImage(
    format: String,
    width: Int,
    height: Int,
): ByteArray =
    ByteArrayOutputStream()
        .also { ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, it) }
        .toByteArray()

/** WebP 파일 머리(RIFF 헤더와 [chunk] 이름, 청크 길이 자리)까지. */
internal fun webpHeader(chunk: String): ByteArray =
    "RIFF".toByteArray() + ByteArray(4) + "WEBP$chunk".toByteArray() + ByteArray(4)

/** [value]를 리틀엔디언 [size]바이트로. */
internal fun littleEndian(
    value: Long,
    size: Int,
): ByteArray = ByteArray(size) { ((value shr (8 * it)) and 0xff).toByte() }
