package log.haeti.buildlogic

import java.io.File

/**
 * 이미지의 가로·세로 픽셀.
 *
 * @param width 가로 픽셀
 * @param height 세로 픽셀
 */
internal data class ImageSize(
    val width: Int,
    val height: Int,
)

private val MARKDOWN_IMAGE = Regex("""!\[([^\]]*)]\(([^)\s]+)\)(?:\{([^}]*)})?""")
private val CODE_FENCE = Regex("""^\s*(```|~~~)""")
private val DARK_ATTRIBUTE = Regex("""dark=(?:"([^"]*)"|([^\s}]+))""")
private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

/**
 * 글 디렉터리 안 이미지에 `size=가로x세로` 지시어를 덧붙인다. 앱은 이 비율로 이미지 자리를 먼저 잡아,
 * 이미지가 도착해도 아래 글이 밀리지 않는다. 배포용 복사본에만 쓰고 원본 글은 건드리지 않는다.
 * 코드 블록 안 예시, 없는 파일, 글 디렉터리 밖 파일, 모르는 형식, 이미 `size`가 있는 이미지는 그대로 둔다.
 *
 * @param markdown 글 본문(프론트매터 포함)
 * @param postDirectory 이미지 상대 경로의 기준인 글 디렉터리
 */
internal fun withImageSizes(
    markdown: String,
    postDirectory: File,
): String {
    val root = postDirectory.canonicalFile
    var inFence = false
    return markdown.split("\n").joinToString("\n") { line ->
        when {
            CODE_FENCE.containsMatchIn(line) -> line.also { inFence = !inFence }
            inFence -> line
            else -> MARKDOWN_IMAGE.replace(line) { withSize(it, root) }
        }
    }
}

/**
 * 이미지 하나에 크기를 덧붙인다. 다크 이미지(`dark=`)가 있으면 두 비율이 같을 때만 적는다.
 * 다르면 한쪽 비율로 자리를 잡는 순간 다른 테마에서 그림이 찌그러진다.
 */
private fun withSize(
    match: MatchResult,
    root: File,
): String {
    val (alt, path, attributes) = match.destructured
    if ("size=" in attributes) return match.value
    val size = sizeOf(root, path) ?: return match.value
    val darkPath = DARK_ATTRIBUTE.find(attributes)?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } }
    if (darkPath != null) {
        val dark = sizeOf(root, darkPath)
        val sameRatio = dark != null && dark.width.toLong() * size.height == dark.height.toLong() * size.width
        if (!sameRatio) return match.value
    }
    val joined = listOf(attributes.trim(), "size=${size.width}x${size.height}").filter { it.isNotEmpty() }
    return "![$alt]($path){${joined.joinToString(" ")}}"
}

/** 글 디렉터리 안 [path] 이미지의 크기. 없거나 디렉터리 밖이거나 형식을 모르면 null. */
private fun sizeOf(
    root: File,
    path: String,
): ImageSize? =
    root.resolve(path).canonicalFile
        .takeIf { it.isFile && it.startsWith(root) }
        ?.let { readImageSize(it.readBytes()) }

/** 파일 머리만 읽어 PNG·GIF·JPEG·WebP의 크기를 돌려준다. 모르는 형식이면 null. ImageIO는 WebP를 읽지 못한다. */
internal fun readImageSize(bytes: ByteArray): ImageSize? =
    when {
        bytes.startsWith(PNG_SIGNATURE) && bytes.size >= 24 -> ImageSize(bytes.be(16, 4), bytes.be(20, 4))
        bytes.ascii(0, 3) == "GIF" && bytes.size >= 10 -> ImageSize(bytes.le(6, 2), bytes.le(8, 2))
        bytes.size >= 4 && bytes.u8(0) == 0xFF && bytes.u8(1) == 0xD8 -> jpegSize(bytes)
        bytes.ascii(0, 4) == "RIFF" && bytes.ascii(8, 4) == "WEBP" -> webpSize(bytes)
        else -> null
    }

/** WebP 세 방식(손실 VP8, 무손실 VP8L, 확장 VP8X)의 헤더에서 크기를 읽는다. */
private fun webpSize(bytes: ByteArray): ImageSize? =
    when (bytes.ascii(12, 4)) {
        "VP8 " -> if (bytes.size >= 30) ImageSize(bytes.le(26, 2) and 0x3FFF, bytes.le(28, 2) and 0x3FFF) else null
        "VP8L" ->
            if (bytes.size >= 25) {
                val bits = bytes.le(21, 4)
                ImageSize((bits and 0x3FFF) + 1, ((bits shr 14) and 0x3FFF) + 1)
            } else {
                null
            }
        "VP8X" -> if (bytes.size >= 30) ImageSize(bytes.le(24, 3) + 1, bytes.le(27, 3) + 1) else null
        else -> null
    }

/** JPEG 세그먼트를 따라가 첫 프레임 헤더(SOF)에서 크기를 읽는다. */
private fun jpegSize(bytes: ByteArray): ImageSize? {
    var offset = 2
    while (offset + 9 < bytes.size) {
        if (bytes.u8(offset) != 0xFF) return null
        val marker = bytes.u8(offset + 1)
        val isFrame = marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC
        if (isFrame) return ImageSize(bytes.be(offset + 7, 2), bytes.be(offset + 5, 2))
        offset += 2 + bytes.be(offset + 2, 2)
    }
    return null
}

private fun ByteArray.startsWith(prefix: ByteArray): Boolean = size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

private fun ByteArray.ascii(
    offset: Int,
    length: Int,
): String = if (size >= offset + length) String(this, offset, length, Charsets.ISO_8859_1) else ""

private fun ByteArray.u8(index: Int): Int = this[index].toInt() and 0xFF

private fun ByteArray.le(
    offset: Int,
    length: Int,
): Int = (0 until length).sumOf { u8(offset + it) shl (8 * it) }

private fun ByteArray.be(
    offset: Int,
    length: Int,
): Int = (0 until length).fold(0) { acc, i -> (acc shl 8) or u8(offset + i) }
