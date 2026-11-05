package log.haeti.core.markdown

import androidx.compose.runtime.Composable
import log.haeti.core.markdown.model.ImageLoad

/**
 * 마크다운 안의 상대경로 이미지를 해석하는 인터페이스.
 * 플랫폼별 리소스 로딩 방식(jvm 파일 IO, wasmJs 리소스 번들 등)은 구현체가 책임진다.
 */
fun interface ImageResolver {
    /**
     * 마크다운 안의 상대경로 이미지(예: `images/foo.webp`)를 읽은 상태를 돌려준다.
     *
     * @param path 마크다운 문서 기준 상대경로
     * @return 읽는 중이면 [ImageLoad.Loading], 다 읽으면 [ImageLoad.Loaded], 실패하면 [ImageLoad.Failed]
     */
    @Composable
    fun load(path: String): ImageLoad
}
