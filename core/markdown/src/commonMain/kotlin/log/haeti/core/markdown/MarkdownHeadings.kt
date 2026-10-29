package log.haeti.core.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.LocalMarkdownExtendedSpans
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.MarkdownHeader

/**
 * 헤딩 앞뒤 여백. 렌더러의 블록 간격은 모든 블록에 하나의 값만 주므로,
 * 헤딩만 위쪽을 크게 벌려 새 단락이 시작됨을 눈에 띄게 한다.
 */
private val HeadingTopSpacing = mapOf(1 to 48.dp, 2 to 44.dp, 3 to 32.dp, 4 to 24.dp)

/**
 * 레벨별 위 여백을 붙인 헤딩. `MarkdownBody`가 `markdownComponents(heading1..4)`로 연결한다.
 *
 * @param model 렌더러가 넘겨주는 현재 헤딩 노드
 * @param level 헤딩 레벨(1~4)
 * @param compact 좁은 화면용 타이포를 쓰는지. 헤딩 안 인라인 코드 칩 위치를 맞추는 데 쓴다
 */
@Composable
internal fun SpacedHeading(
    model: MarkdownComponentModel,
    level: Int,
    compact: Boolean,
) {
    val style: TextStyle =
        when (level) {
            1 -> model.typography.h1
            2 -> model.typography.h2
            3 -> model.typography.h3
            else -> model.typography.h4
        }
    // 문서 첫 블록이면 앞선 머리말·커버가 이미 여백을 만들었으므로 위 여백을 주지 않는다.
    val top: Dp = if (model.node.startOffset == 0) 0.dp else HeadingTopSpacing[level] ?: 28.dp
    Box(modifier = Modifier.padding(top = top, bottom = 4.dp)) {
        CompositionLocalProvider(LocalMarkdownExtendedSpans provides rememberInlineCodeChips(compact, level)) {
            MarkdownHeader(content = model.content, node = model.node, style = style)
        }
    }
}
