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
private val CODE_FENCE = Regex("""^ {0,3}(`{3,}|~{3,})(.*)$""")

/** 지시어 속성 `key=value`. 따옴표 값을 통째로 읽어 캡션 속 `size=` 같은 글자를 속성으로 보지 않는다. 앱의 파서와 같다. */
private val ATTRIBUTE = Regex("""(\w+)=(?:"([^"]*)"|(\S+))""")
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
    val lines = markdown.split("\n")
    val prose = proseLineFlags(lines)
    // 지시어는 경로별로 하나만 남는다. 지시어가 있는 경로를 다시 쓴 곳에 크기만 붙이면 앞 지시어를 덮어쓴다.
    val directed =
        lines.filterIndexed { i, _ -> prose[i] }
            .flatMap { MARKDOWN_IMAGE.findAll(it) }
            .filter { it.groupValues[3].isNotBlank() }
            .map { it.groupValues[2] }
            .toSet()
    return lines
        .mapIndexed { i, line -> if (prose[i]) MARKDOWN_IMAGE.replace(line) { withSize(it, root, directed) } else line }
        .joinToString("\n")
}

/**
 * 줄마다 코드 블록 밖이면 true. 닫는 펜스는 여는 펜스와 같은 문자로 같거나 더 길고 뒤에 공백만 있어야 한다(CommonMark).
 * 그래야 백틱 네 개 블록 안의 세 개짜리 예시가 블록을 닫지 않는다.
 */
private fun proseLineFlags(lines: List<String>): List<Boolean> {
    var open: String? = null
    return lines.map { line ->
        val fence = CODE_FENCE.matchEntire(line.trimEnd('\r'))
        val current = open
        when {
            current == null && fence != null -> false.also { open = fence.groupValues[1] }
            current == null -> true
            else -> {
                val marker = fence?.groupValues?.get(1)
                val closes = marker != null && marker[0] == current[0] && marker.length >= current.length
                if (closes && fence.groupValues[2].isBlank()) open = null
                false
            }
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
    directed: Set<String>,
): String {
    val (alt, path, attributes) = match.destructured
    val keys = ATTRIBUTE.findAll(attributes).associate { it.groupValues[1] to it.groupValues[2].ifEmpty { it.groupValues[3] } }
    if ("size" in keys || (attributes.isBlank() && path in directed)) return match.value
    val size = sizeOf(root, path) ?: return match.value
    val darkPath = keys["dark"]
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
