package log.haeti.buildlogic.site

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

private const val JPEG_QUALITY = 0.85f
private val INK = Color(0x16181D)
private val INK_MUTED = Color(0x5F6675)
private val BRAND = Color(0x2B6BEF)
private val BACKGROUND = Color(0xFCFCFD)

/**
 * 글 썸네일을 미리보기용 JPG로 바꾼다. 폭은 [OG_WIDTH] 이하로 줄이고 투명 영역은 흰색으로 채운다.
 * 카카오톡 등 일부 서비스가 webp 미리보기를 못 띄우므로 형식을 JPG로 통일한다.
 *
 * @param source 썸네일 파일 (png·jpg·webp·gif)
 * @param output 쓸 JPG 파일
 */
internal fun writeThumbnailOg(
    source: File,
    output: File,
) {
    val image = readImage(source) ?: error("썸네일을 읽지 못했습니다: $source")
    val width = minOf(image.width, OG_WIDTH)
    val height = (image.height.toLong() * width / image.width).toInt().coerceAtLeast(1)
    val canvas = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    canvas.paint {
        color = Color.WHITE
        fillRect(0, 0, width, height)
        drawImage(image, 0, 0, width, height, null)
    }
    writeJpeg(canvas, output)
}

/** 썸네일 없는 글의 미리보기. 블로그와 같은 카테고리 바탕에 같은 코드 미니맵을 그린 PNG다. */
internal fun writeCoverOg(
    post: SitePost,
    output: File,
) {
    val palette = coverPalette(post.category)
    val canvas = BufferedImage(OG_WIDTH, OG_HEIGHT, BufferedImage.TYPE_INT_RGB)
    canvas.paint {
        color = palette.tint
        fillRect(0, 0, OG_WIDTH, OG_HEIGHT)
        drawCodeMinimap(post.slug, palette.accent, OG_WIDTH.toFloat(), OG_HEIGHT.toFloat())
    }
    ImageIO.write(canvas, "png", output)
}

/**
 * 홈·고정 페이지의 미리보기. 왼쪽에 워드마크와 소개, 오른쪽에 브랜드 색 코드 미니맵 카드를 둔다.
 *
 * @param site 사이트 이름·소개
 * @param bold 워드마크 글꼴 (Pretendard Bold)
 * @param regular 소개 글꼴 (Pretendard Regular)
 * @param output 쓸 PNG 파일
 */
internal fun writeSiteOg(
    site: SiteInfo,
    bold: File,
    regular: File,
    output: File,
) {
    val canvas = BufferedImage(OG_WIDTH, OG_HEIGHT, BufferedImage.TYPE_INT_RGB)
    canvas.paint {
        color = BACKGROUND
        fillRect(0, 0, OG_WIDTH, OG_HEIGHT)
        font = Font.createFont(Font.TRUETYPE_FONT, bold).deriveFont(92f)
        val name = site.name.substringBefore('.')
        color = INK
        drawString(name, 96, 300)
        color = BRAND
        drawString(site.name.removePrefix(name), 96 + fontMetrics.stringWidth(name), 300)
        font = Font.createFont(Font.TRUETYPE_FONT, regular).deriveFont(34f)
        color = INK_MUTED
        site.description.split(". ").forEachIndexed { line, text ->
            drawString(text.trimEnd('.'), 100, 380 + line * 52)
        }
        val card = RoundRectangle2D.Float(760f, 115f, 344f, 400f, 40f, 40f)
        color = Color(0xE6EEFD)
        fill(card)
        translate(760.0, 115.0)
        drawCodeMinimap(site.name, Color(0x2563D9), 344f, 400f)
    }
    ImageIO.write(canvas, "png", output)
}

private fun readImage(file: File): BufferedImage? {
    if (file.extension.lowercase() != "webp") return ImageIO.read(file)
    // 플러그인 등록(ServiceLoader)은 Gradle 클래스로더에서 보이지 않을 수 있어 리더를 직접 만든다.
    val reader = WebPImageReaderSpi().createReaderInstance()
    return ImageIO.createImageInputStream(file).use { input ->
        reader.input = input
        reader.read(0).also { reader.dispose() }
    }
}

private fun writeJpeg(
    image: BufferedImage,
    output: File,
) {
    val writer = ImageIO.getImageWritersByFormatName("jpg").next()
    val param = writer.defaultWriteParam.apply {
        compressionMode = ImageWriteParam.MODE_EXPLICIT
        compressionQuality = JPEG_QUALITY
    }
    ImageIO.createImageOutputStream(output).use { stream ->
        writer.output = stream
        writer.write(null, IIOImage(image, null, null), param)
    }
    writer.dispose()
}

private fun BufferedImage.paint(block: Graphics2D.() -> Unit) {
    val graphics = createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    try {
        graphics.block()
    } finally {
        graphics.dispose()
    }
}
