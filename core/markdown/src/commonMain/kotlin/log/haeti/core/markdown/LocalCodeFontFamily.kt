package log.haeti.core.markdown

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily

/**
 * 코드 블록·인라인 코드에 쓰는 고정폭 글꼴. [MarkdownBody]가 호출부에서 받은 값을 제공한다.
 * 웹에는 시스템 고정폭 글꼴이 없어서 기본값([FontFamily.Monospace])을 그대로 두면 본문 글꼴로 그려진다.
 */
internal val LocalCodeFontFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Monospace }
