package log.haeti.core.markdown

import androidx.compose.runtime.Composable
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.MarkdownParagraph
import log.haeti.core.markdown.model.ImageDirective
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

/**
 * 문단 커스텀 컴포넌트. `::youtube[영상ID]` 한 줄이면 [YouTubeEmbed]로, 이미지 하나만 담고 있으면 지시어를 반영한
 * [MarkdownImageComponent]로, 아니면 기본 문단 렌더링으로 위임한다.
 *
 * 렌더러 디스패치는 최상위 노드만 보기 때문에 `components.image`는 문단 속
 * 이미지에 호출되지 않는다. 단독 이미지 줄도 PARAGRAPH > IMAGE로 파싱되므로
 * 문단 단계에서 가로채는 것이 유일한 진입점이다.
 */
@Composable
internal fun MarkdownParagraphComponent(
    model: MarkdownComponentModel,
    imageResolver: ImageResolver,
    directives: Map<String, ImageDirective>,
    isDarkPage: Boolean,
) {
    val videoId = youtubeVideoIdOrNull(model.node.getTextInNode(model.content).toString())
    val image = model.node.singleImageChildOrNull()
    if (videoId != null) {
        YouTubeEmbed(videoId)
    } else if (image != null) {
        MarkdownImageComponent(model.copy(node = image), imageResolver, directives, isDarkPage)
    } else {
        MarkdownParagraph(model.content, model.node, style = model.typography.paragraph)
    }
}

/** 공백·개행을 제외한 유일한 자식이 IMAGE면 그 노드를 돌려준다. */
private fun ASTNode.singleImageChildOrNull(): ASTNode? {
    val meaningful =
        children.filter {
            it.type != MarkdownTokenTypes.WHITE_SPACE && it.type != MarkdownTokenTypes.EOL
        }
    return meaningful.singleOrNull()?.takeIf { it.type == MarkdownElementTypes.IMAGE }
}
