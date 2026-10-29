package log.haeti.core.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * 마크다운 안의 상대경로 이미지를 해석하는 인터페이스.
 * 플랫폼별 리소스 로딩 방식(jvm 파일 IO, wasmJs 리소스 번들 등)은 구현체가 책임진다.
 */
fun interface ImageResolver {
    /**
     * 마크다운 안의 상대경로 이미지(예: `images/foo.webp`)를 [Painter]로 해석한다.
     *
     * @param path 마크다운 문서 기준 상대경로
     * @return 해석된 [Painter]. 해석에 실패하면 null
     */
    @Composable
    fun painterFor(path: String): Painter?
}
