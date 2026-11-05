package log.haeti.admin.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.markdown.model.ImageLoad
import log.haeti.core.markdown.rememberEncodedImagePainter
import java.nio.file.Path

/**
 * 글 디렉터리 기준 상대경로 이미지를 디스크에서 읽어 그린다.
 * 블로그와 같은 Skia 디코더를 써서 webp도 보이고 애니메이션 GIF·WebP도 재생한다.
 * 파일이 없거나 해석할 수 없으면 [ImageLoad.Failed]라서, 미리보기에서 경로 오타를 바로 알아챈다.
 *
 * @param postDir 현재 열려 있는 글의 디렉터리
 */
class AdminImageResolver(
    private val postDir: Path,
) : ImageResolver {
    @Composable
    override fun load(path: String): ImageLoad {
        val bytes =
            remember(postDir, path) {
                postDir
                    .resolve(path)
                    .toFile()
                    .takeIf { it.isFile }
                    ?.readBytes()
            }
        val painter = bytes?.let { rememberEncodedImagePainter(it) }
        return painter?.let { ImageLoad.Loaded(it) } ?: ImageLoad.Failed
    }
}
