package log.haeti.core.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxTheme
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

/** 코드 카드 배경. 페이지가 라이트여도 항상 이 값 (발표자료 무드 고정). [haetiMarkdownColors]도 재사용한다. */
internal val HAETI_CODE_CARD_BACKGROUND = Color(0xFF232329)

/** 코드 카드 모서리 반경. [com.mikepenz.markdown.model.markdownDimens]의 codeBackgroundCornerSize로 전달된다. */
internal val HAETI_CODE_CARD_RADIUS = 16.dp

/** 코드 카드 내부 패딩. [com.mikepenz.markdown.model.markdownPadding]의 codeBlock으로 전달된다. */
internal val HAETI_CODE_CARD_PADDING = PaddingValues(20.dp)

private val CODE_TEXT_COLOR = Color(0xFFF0F0F3)

/** 언어 라벨·복사 버튼 아이콘·"복사됨" 텍스트가 공유하는 무채색 톤. */
internal val CODE_LABEL_COLOR = Color(0xFF8A8A93)

/**
 * JetBrains IDE 팔레트로 매핑한 커스텀 Highlights 테마.
 * `multilineComment`는 팔레트에 별도 색이 없어 `comment`와 같은 색을 쓴다.
 * 팔레트의 함수명 색(#56A8F5)은 매핑하지 않는다. 이 라이브러리는 함수명을 따로 인식하지 않아
 * [SyntaxTheme]에 대응 필드가 없다.
 *
 * `mark`는 팔레트의 타입 색(#C77DBB) 대신 [CODE_TEXT_COLOR]로 강제한다. 이 라이브러리의 mark
 * 카테고리는 실제로는 타입명이 아니라 괄호·중괄호·꺾쇠·등호 등 연산자/구두점 문자를 담당하므로,
 * 원래 값(핑크)을 쓰면 괄호만 다른 색으로 튀어 "안 보인다"는 착시를 만든다.
 */
private val HAETI_SYNTAX_THEME =
    SyntaxTheme(
        key = "haeti",
        code = 0xFFF0F0F3.toInt(),
        keyword = 0xFFCF8E6D.toInt(),
        string = 0xFF6AAB73.toInt(),
        literal = 0xFF2AACB8.toInt(),
        comment = 0xFF8A8A93.toInt(),
        metadata = 0xFFC77DBB.toInt(),
        multilineComment = 0xFF8A8A93.toInt(),
        punctuation = 0xFFF0F0F3.toInt(),
        mark = 0xFFF0F0F3.toInt(),
    )

/** 코드 본문 스타일. 카드가 항상 다크라 [androidx.compose.material3.MaterialTheme] 비의존 고정값을 쓴다. */
private fun codeTextStyle(
    compact: Boolean,
    fontFamily: FontFamily,
): TextStyle {
    val size = if (compact) 13.sp else 14.sp
    return TextStyle(
        fontFamily = fontFamily,
        fontSize = size,
        lineHeight = size * 1.6f,
        color = CODE_TEXT_COLOR,
    )
}

/** 코드펜스 언어 라벨·복사 버튼 상태 텍스트 스타일 (우상단 오버레이). */
internal val CODE_LABEL_STYLE =
    TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = CODE_LABEL_COLOR,
    )

/**
 * 들여쓰기 코드블록(4-space indent)을 렌더링한다.
 * `MarkdownBody`가 `markdownComponents(codeBlock = ...)`로 연결한다.
 *
 * @param compact 좁은 화면(<600dp)용 축소 코드 폰트(13sp) 적용 여부
 */
@Composable
internal fun HaetiCodeBlock(
    model: MarkdownComponentModel,
    compact: Boolean,
) {
    val codeText = remember(model.node, model.content) { model.node.getTextInNode(model.content).toString() }
    HaetiCodeCard(model, compact, codeText) { builder, style ->
        MarkdownHighlightedCodeBlock(
            content = model.content,
            node = model.node,
            style = style,
            highlightsBuilder = builder,
            showHeader = false, // 라벨은 HaetiCodeCard가 직접 오버레이로 그린다
        )
    }
}

/**
 * 코드펜스(triple-backtick)를 렌더링한다.
 * `MarkdownBody`가 `markdownComponents(codeFence = ...)`로 연결한다.
 *
 * @param compact 좁은 화면(<600dp)용 축소 코드 폰트(13sp) 적용 여부
 */
@Composable
internal fun HaetiCodeFence(
    model: MarkdownComponentModel,
    compact: Boolean,
) {
    val codeText = remember(model.node, model.content) { fenceCodeText(model) }
    HaetiCodeCard(model, compact, codeText) { builder, style ->
        MarkdownHighlightedCodeFence(
            content = model.content,
            node = model.node,
            style = style,
            highlightsBuilder = builder,
            showHeader = false, // 라벨은 HaetiCodeCard가 직접 오버레이로 그린다
        )
    }
}

/** 코드펜스 원문에서 여는/닫는 백틱 줄(` ```lang `, ` ``` `)을 제거한 순수 코드 텍스트를 얻는다. */
private fun fenceCodeText(model: MarkdownComponentModel): String {
    val lines =
        model.node
            .getTextInNode(model.content)
            .toString()
            .lines()
    return if (lines.size >= 2) lines.drop(1).dropLast(1).joinToString("\n") else lines.joinToString("\n")
}

/**
 * 카드 배경·radius·padding·가로 스크롤(줄바꿈 없음)은 [content]로 넘기는 하이라이트 컴포저블이
 * ambient `LocalMarkdownColors`/`Dimens`/`Padding`(모두 `MarkdownBody`가 고정 다크로 설정)으로
 * 이미 그린다. 여기서는 커스텀 테마의 [Highlights.Builder]를 만들고 언어 라벨·복사 버튼만 얹는다.
 *
 * @param codeText 복사 버튼이 클립보드로 보낼 순수 코드 텍스트
 */
@Composable
private fun HaetiCodeCard(
    model: MarkdownComponentModel,
    compact: Boolean,
    codeText: String,
    content: @Composable (Highlights.Builder, TextStyle) -> Unit,
) {
    val builder = remember { Highlights.Builder().theme(HAETI_SYNTAX_THEME) }
    val fontFamily = LocalCodeFontFamily.current
    val style = remember(compact, fontFamily) { codeTextStyle(compact, fontFamily) }
    val language =
        remember(model.node, model.content) {
            model.node
                .findChildOfType(MarkdownTokenTypes.FENCE_LANG)
                ?.getTextInNode(model.content)
                ?.toString()
        }

    Box {
        content(builder, style)
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!language.isNullOrBlank()) {
                Text(text = language, style = CODE_LABEL_STYLE.copy(fontFamily = LocalCodeFontFamily.current))
            }
            CodeCopyButton(text = codeText)
        }
    }
}
