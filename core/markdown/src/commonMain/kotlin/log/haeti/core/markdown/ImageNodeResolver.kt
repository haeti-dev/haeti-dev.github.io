package log.haeti.core.markdown

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

/**
 * IMAGE AST 노드에서 링크 경로를 꺼낸다.
 * 렌더러의 resolveImageLink가 internal이라 공개 AST API로 직접 파싱한다.
 *
 * @param content 마크다운 전문
 * @return 이미지 경로. 구조가 예상과 다르면 null
 */
internal fun ASTNode.imagePath(content: String): String? {
    val link = findChildOfType(MarkdownElementTypes.INLINE_LINK) ?: this
    val destination = link.findChildOfType(MarkdownElementTypes.LINK_DESTINATION) ?: return null
    return destination.getTextInNode(content).toString()
}

/**
 * IMAGE AST 노드에서 대체텍스트를 꺼낸다.
 *
 * @param content 마크다운 전문
 * @return 대체텍스트. 없으면 빈 문자열
 */
internal fun ASTNode.imageAlt(content: String): String {
    val link = findChildOfType(MarkdownElementTypes.INLINE_LINK) ?: return ""
    val text = link.findChildOfType(MarkdownElementTypes.LINK_TEXT) ?: return ""
    return text
        .getTextInNode(content)
        .toString()
        .removePrefix("[")
        .removeSuffix("]")
}
