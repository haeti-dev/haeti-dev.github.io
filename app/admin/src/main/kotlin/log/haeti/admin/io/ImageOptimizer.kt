package log.haeti.admin.io

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

/** 이 폭을 넘는 이미지는 줄인다. 본문 최대 폭(커버 800dp)의 레티나 2배다. */
internal const val MAX_IMAGE_WIDTH = 1600

/** 손실 webp 품질(0~100). */
internal const val WEBP_QUALITY = 85

/** 블로그에 넣을 수 있는 이미지 확장자. */
internal val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "gif")

/**
 * 글에 넣을 이미지 한 장.
 *
 * @param baseName 확장자를 뺀 파일 이름
 * @param extension 저장할 확장자
 * @param bytes 저장할 내용
 */
class ImageFile(
    val baseName: String,
    val extension: String,
    val bytes: ByteArray,
)

/**
 * 블로그용으로 이미지를 다듬는다. 폭이 [MAX_IMAGE_WIDTH]를 넘으면 비율대로 줄이고 webp로 저장한다.
 *
 * 휴대폰 사진의 EXIF 회전은 Skia가 디코딩할 때 이미 적용하므로 다시 인코딩한 webp도 방향이 맞다.
 * GIF는 움직임이 사라지므로 그대로 둔다. 줄일 필요 없는 webp도 다시 압축하지 않는다.
 * 변환 결과가 원본보다 크면(이미 잘 압축된 작은 이미지) 원본을 쓴다. 해석할 수 없는 파일도 원본 그대로다.
 *
 * @param source 원본 이미지
 */
internal fun optimizeImage(source: ImageFile): ImageFile {
    if (source.extension == "gif") return source
    val image = runCatching { Image.makeFromEncoded(source.bytes) }.getOrNull() ?: return source
    val webp =
        image.use {
            val needsResize = image.width > MAX_IMAGE_WIDTH
            if (!needsResize && source.extension == "webp") return source
            val encoded = image.toWebp(minOf(image.width, MAX_IMAGE_WIDTH)) ?: return source
            if (!needsResize && encoded.size >= source.bytes.size) return source
            encoded
        }
    return ImageFile(source.baseName, "webp", webp)
}

/** 비율을 지켜 [width]로 그린 뒤 webp로 인코딩한다. 축소 계단 현상을 줄이려 밉맵 선형 보간을 쓴다. */
private fun Image.toWebp(width: Int): ByteArray? {
    val height = (this.height.toLong() * width / this.width).toInt().coerceAtLeast(1)
    return Surface.makeRasterN32Premul(width, height).use { surface ->
        surface.canvas.drawImageRect(
            this,
            Rect.makeWH(this.width.toFloat(), this.height.toFloat()),
            Rect.makeWH(width.toFloat(), height.toFloat()),
            FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR),
            null,
            true,
        )
        surface.makeImageSnapshot().use { it.encodeToData(EncodedImageFormat.WEBP, WEBP_QUALITY)?.bytes }
    }
}
