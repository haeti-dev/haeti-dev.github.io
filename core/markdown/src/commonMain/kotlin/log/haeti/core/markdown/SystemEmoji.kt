package log.haeti.core.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.em
import com.mikepenz.markdown.model.DefaultMarkdownInlineContent
import com.mikepenz.markdown.model.MarkdownAnnotator
import com.mikepenz.markdown.model.MarkdownInlineContent
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import org.intellij.markdown.MarkdownTokenTypes

/** 이모지 그림의 폭·높이. 글자 크기를 따라가므로 헤딩에서도 비율이 맞는다. */
private val EmojiSize = 1.2.em

/**
 * 운영체제가 그린 [emoji]의 PNG 바이트. 직접 그릴 수 있는 플랫폼이면 null을 돌려준다.
 *
 * 웹 캔버스의 Skia는 운영체제 글꼴에 접근하지 못한다. 그대로 두면 Compose가 Noto Color Emoji를 내려받아
 * 그리므로, 브라우저에 맡겨 그린 그림을 글 사이에 끼워 넣는다.
 */
internal expect fun systemEmojiPng(emoji: String): ByteArray?

/** [markdown]에 나오는 이모지를 운영체제 그림으로 그리는 인라인 콘텐츠. 그릴 수 없는 플랫폼이면 비어 있다. */
@Composable
internal fun rememberEmojiInlineContent(markdown: String): MarkdownInlineContent =
    remember(markdown) {
        val emoji = splitEmoji(markdown).filter { it.isEmoji }.map { it.text }.distinct()
        DefaultMarkdownInlineContent(
            emoji.filter { systemEmojiPng(it) != null }.associate { emojiId(it) to content(it) },
        )
    }

/**
 * 글 속 이모지를 [inlineContent]의 그림 자리로 바꾸는 annotator. 복사할 때는 원래 글자가 나온다.
 *
 * @param inlineContent [rememberEmojiInlineContent]가 만든 인라인 콘텐츠
 */
internal fun emojiAnnotator(inlineContent: MarkdownInlineContent): MarkdownAnnotator {
    val known = inlineContent.inlineContent.keys
    return markdownAnnotator { content, child ->
        if (child.type != MarkdownTokenTypes.TEXT) return@markdownAnnotator false
        // 렌더러의 기본 TEXT 처리와 같은 문자열을 써야 `\*` 같은 이스케이프가 그대로 드러나지 않는다.
        val segments = splitEmoji(child.getUnescapedTextInNode(content))
        if (segments.none { emojiId(it.text) in known }) return@markdownAnnotator false
        segments.forEach { segment ->
            val id = emojiId(segment.text)
            if (id in known) appendInlineContent(id, segment.text) else append(segment.text)
        }
        true
    }
}

private fun emojiId(emoji: String): String = "emoji:$emoji"

private fun content(emoji: String): InlineTextContent =
    InlineTextContent(Placeholder(EmojiSize, EmojiSize, PlaceholderVerticalAlign.TextCenter)) {
        val bytes = remember(emoji) { systemEmojiPng(emoji) } ?: return@InlineTextContent
        val painter = rememberEncodedImagePainter(bytes) ?: return@InlineTextContent
        Image(painter = painter, contentDescription = emoji, modifier = Modifier.fillMaxSize())
    }
