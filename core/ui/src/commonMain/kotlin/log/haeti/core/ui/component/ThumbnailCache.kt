package log.haeti.core.ui.component

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

/** 썸네일을 디코딩할 폭 단계(px). 비슷한 크기의 자리끼리 같은 비트맵을 쓴다. */
private val WIDTH_BUCKETS = intArrayOf(256, 512, 1024, 2048)

/** 캐시할 최대 개수. 넘치면 가장 먼저 넣은 것부터 버린다. */
private const val MAX_ENTRIES = 64

/**
 * 표시 폭에 맞춰 줄인 썸네일을 기억해 둔다.
 *
 * 목록이 걸러졌다 돌아올 때(검색·카테고리) 행마다 1600px 원본을 다시 풀면, 그동안 대체 그림이 깜빡이고
 * 웹(단일 스레드)은 그만큼 멈춘다. 한 번 줄여 둔 비트맵을 다시 쓰면 즉시 그려진다.
 */
internal object ThumbnailCache {
    private val entries = LinkedHashMap<String, ImageBitmap>()

    /** [key]로 기억해 둔 비트맵. 없으면 null. */
    operator fun get(key: String): ImageBitmap? = entries[key]

    /** [key]로 [bitmap]을 기억한다. */
    operator fun set(
        key: String,
        bitmap: ImageBitmap,
    ) {
        entries[key] = bitmap
        if (entries.size > MAX_ENTRIES) entries.remove(entries.keys.first())
    }

    /** [widthPx]를 담을 수 있는 가장 작은 폭 단계. */
    fun bucketFor(widthPx: Int): Int = WIDTH_BUCKETS.firstOrNull { it >= widthPx } ?: WIDTH_BUCKETS.last()
}

/**
 * 인코딩된 이미지를 폭 [maxWidthPx] 이하로 줄여 디코딩한다. 이미 작으면 그대로다.
 *
 * @param bytes 이미지 파일 내용
 * @param maxWidthPx 최대 폭(px)
 */
internal fun decodeThumbnail(
    bytes: ByteArray,
    maxWidthPx: Int,
): ImageBitmap {
    val image = Image.makeFromEncoded(bytes)
    if (image.width <= maxWidthPx) return image.toComposeImageBitmap()
    val height = (image.height.toLong() * maxWidthPx / image.width).toInt().coerceAtLeast(1)
    val surface = Surface.makeRasterN32Premul(maxWidthPx, height)
    surface.canvas.drawImageRect(
        image,
        Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
        Rect.makeWH(maxWidthPx.toFloat(), height.toFloat()),
        FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR),
        null,
        true,
    )
    return surface.makeImageSnapshot().toComposeImageBitmap()
}
