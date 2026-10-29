package log.haeti.core.markdown

import androidx.compose.runtime.Composable
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer

/**
 * 문장 속 인라인 이미지용 변환기. 렌더러 기본값이 NoOp이라 이것을 넘기지 않으면
 * 인라인 이미지는 아무것도 그려지지 않는다. 지시어는 단독 이미지 문단에만 적용되므로
 * 여기서는 원본 크기 렌더만 담당한다.
 *
 * @param resolver 상대경로를 Painter로 변환하는 resolver
 */
internal class HaetiImageTransformer(
    private val resolver: ImageResolver,
) : ImageTransformer {
    @Composable
    override fun transform(link: String): ImageData? = resolver.painterFor(link)?.let { ImageData(painter = it) }
}
