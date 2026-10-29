package log.haeti.admin.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.markdown.rememberEncodedImagePainter
import java.nio.file.Path

/**
 * 글 디렉터리 기준 상대경로 이미지를 디스크에서 읽어 [Painter]로 변환한다.
 * 블로그와 같은 Skia 디코더를 써서 webp도 보이고 애니메이션 GIF·WebP도 재생한다. 파일이 없으면 null이다.
 *
 * @param postDir 현재 열려 있는 글의 디렉터리
 */
class AdminImageResolver(
    private val postDir: Path,
) : ImageResolver {
    @Composable
    override fun painterFor(path: String): Painter? {
        val bytes =
            remember(postDir, path) {
                postDir
                    .resolve(path)
                    .toFile()
                    .takeIf { it.isFile }
                    ?.readBytes()
            }
        return bytes?.let { rememberEncodedImagePainter(it) }
    }
}
