package log.haeti.core.markdown

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ImageNodeResolverTest {
    private fun findImage(root: ASTNode): ASTNode? {
        if (root.type == MarkdownElementTypes.IMAGE) return root
        for (child in root.children) findImage(child)?.let { return it }
        return null
    }

    @Test
    fun `단독 이미지 라인에서 경로와 대체텍스트를 뽑는다`() {
        val md = "![샘플 이미지](images/sample.webp)"
        val tree = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(md)
        val image = assertNotNull(findImage(tree), "IMAGE 노드를 찾지 못함. AST 구조 확인 필요")
        assertEquals("images/sample.webp", image.imagePath(md))
        assertEquals("샘플 이미지", image.imageAlt(md))
    }
}
