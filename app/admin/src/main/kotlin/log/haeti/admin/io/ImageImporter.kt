package log.haeti.admin.io

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.MultiResolutionImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.text.Normalizer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

private val PASTE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

/**
 * 파일·클립보드 이미지를 블로그용으로 다듬어([optimizeImage]) 글의 `images/`에 저장한다.
 * 모든 메서드는 블로킹이므로 호출부가 IO 디스패처로 감싼다.
 *
 * @param fileStore 실제 파일 쓰기를 맡는 [PostFileStore]
 */
@Inject
@SingleIn(AppScope::class)
class ImageImporter(
    private val fileStore: PostFileStore,
) {
    /** 들어온 이미지를 저장하고 저장된 파일명들을 순서대로 반환한다. 읽지 못한 파일은 건너뛴다. */
    fun import(
        incoming: IncomingImages,
        postDir: Path,
    ): List<String> =
        when (incoming) {
            is IncomingImages.Files -> incoming.files.mapNotNull { runCatching { importFile(it, postDir) }.getOrNull() }
            is IncomingImages.Bitmap -> listOfNotNull(runCatching { importBitmap(incoming.image, postDir) }.getOrNull())
        }

    /** Finder에서 고르거나 끌어 온 이미지 파일을 저장하고 파일명을 반환한다. */
    fun importFile(
        file: File,
        postDir: Path,
    ): String {
        val source = ImageFile(safeImageName(file.nameWithoutExtension), file.extension.lowercase(), file.readBytes())
        return fileStore.saveImage(postDir, optimizeImage(source))
    }

    /** 클립보드 비트맵(스크린샷 등)을 `paste-<날짜시간>` 이름으로 저장하고 파일명을 반환한다. */
    fun importBitmap(
        image: Image,
        postDir: Path,
    ): String {
        val buffer = ByteArrayOutputStream()
        check(ImageIO.write(image.toBufferedImage(), "png", buffer)) { "클립보드 이미지를 PNG로 바꾸지 못했습니다" }
        val png = buffer.toByteArray()
        val baseName = "paste-${LocalDateTime.now().format(PASTE_NAME_FORMAT)}"
        return fileStore.saveImage(postDir, optimizeImage(ImageFile(baseName, "png", png)))
    }
}

/**
 * 마크다운 링크에 그대로 넣을 수 있는 파일 이름. 공백·한글·특수문자는 `-`로 바꾼다.
 * macOS 스크린샷 이름("Screenshot 2026-10-09 at 10.12.03")의 공백은 이미지 문법을 끊는다.
 */
internal fun safeImageName(name: String): String =
    Normalizer
        .normalize(name, Normalizer.Form.NFC)
        .replace(Regex("[^A-Za-z0-9._-]+"), "-")
        .trim('-', '.')
        .lowercase()
        .ifEmpty { "image" }

/** 레티나 스크린샷처럼 해상도가 여럿이면 가장 큰 것을 골라 픽셀 버퍼로 옮긴다. */
private fun Image.toBufferedImage(): BufferedImage {
    val largest = (this as? MultiResolutionImage)?.resolutionVariants?.maxByOrNull { it.getWidth(null) } ?: this
    if (largest is BufferedImage) return largest
    val buffer = BufferedImage(largest.getWidth(null), largest.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    buffer.createGraphics().apply {
        drawImage(largest, 0, 0, null)
        dispose()
    }
    return buffer
}
