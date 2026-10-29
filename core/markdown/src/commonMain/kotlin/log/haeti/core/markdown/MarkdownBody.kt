package log.haeti.core.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.model.rememberMarkdownState
import log.haeti.core.common.text.keepAllMarkdown
import log.haeti.core.designsystem.interaction.SelectableArea
import log.haeti.core.markdown.model.ImageDirective

/**
 * 마크다운 본문을 렌더링한다. 코드 하이라이팅, 이미지 지시어, 콜아웃을 포함한다.
 * 색상은 [MaterialTheme]에서만 가져오며 core:designsystem에는 의존하지 않는다.
 * 단, 코드 카드는 페이지 테마와 무관하게 항상 다크로 고정한다.
 *
 * 이미지는 두 경로로 그려진다:
 * - 단독 이미지 문단: PARAGRAPH > IMAGE로 파싱되므로 문단 컴포넌트가 가로채
 *   지시어(width·align·caption)를 반영한다. 렌더러 디스패치는 최상위 노드만 보므로
 *   `components.image`만으로는 호출되지 않는다.
 * - 문장 속 인라인 이미지: 렌더러 기본 경로(ImageTransformer). 기본값이 NoOp이라
 *   [HaetiImageTransformer]를 반드시 넘겨야 그려진다.
 *
 * @param markdown 렌더링할 마크다운 본문 (프론트매터 제외)
 * @param imageResolver 본문 내 상대경로 이미지를 Painter로 변환하는 resolver
 * @param modifier 루트 컴포저블에 적용할 [Modifier]
 * @param compact 좁은 화면(<600dp)용 축소 타이포 스케일 적용 여부
 * @param codeFontFamily 코드에 쓸 고정폭 글꼴. 웹은 번들 글꼴을 넘겨야 고정폭으로 그려진다
 * @param onOpenPost 본문의 글 링크(`post/<slug>`)를 눌렀을 때 슬러그를 넘기는 콜백
 */
@Composable
fun MarkdownBody(
    markdown: String,
    imageResolver: ImageResolver,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    codeFontFamily: FontFamily = FontFamily.Monospace,
    onOpenPost: (String) -> Unit = {},
) {
    val (cleaned, directives) =
        remember(markdown) {
            val (text, found) = extractImageDirectives(markdown)
            text.keepAllMarkdown() to found
        }
    val transformer: ImageTransformer = remember(imageResolver) { HaetiImageTransformer(imageResolver) }
    val isDarkPage = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val uriHandler = LocalUriHandler.current
    val currentOnOpenPost by rememberUpdatedState(onOpenPost)
    val linkHandler = remember(uriHandler) { PostLinkUriHandler(uriHandler) { currentOnOpenPost(it) } }
    CompositionLocalProvider(
        LocalCodeFontFamily provides codeFontFamily,
        LocalUriHandler provides linkHandler,
    ) {
        SelectableArea {
            HaetiMarkdown(cleaned, directives, imageResolver, transformer, isDarkPage, compact, modifier)
        }
    }
}

/** 렌더러 호출부. 색·타이포·여백·컴포넌트 배선을 한곳에 모은다. */
@Composable
private fun HaetiMarkdown(
    cleaned: String,
    directives: Map<String, ImageDirective>,
    imageResolver: ImageResolver,
    transformer: ImageTransformer,
    isDarkPage: Boolean,
    compact: Boolean,
    modifier: Modifier,
) {
    Markdown(
        // 본문이 바뀌어(언어 전환·어드민 입력) 다시 파싱하는 동안 이전 결과를 그대로 둔다. 기본값이면 빈 상자가 그려져
        // 본문 높이가 0이 되고, 스크롤이 당겨져 읽던 위치를 잃는다.
        markdownState = rememberMarkdownState(cleaned, retainState = true),
        modifier = modifier,
        colors = haetiMarkdownColors(isDarkPage),
        typography = haetiMarkdownTypography(isDarkPage, compact),
        dimens = markdownDimens(codeBackgroundCornerSize = HAETI_CODE_CARD_RADIUS),
        padding = markdownPadding(block = 10.dp, codeBlock = HAETI_CODE_CARD_PADDING),
        extendedSpans = rememberInlineCodeChips(compact),
        imageTransformer = transformer,
        components =
            markdownComponents(
                heading1 = { model -> SpacedHeading(model, level = 1, compact = compact) },
                heading2 = { model -> SpacedHeading(model, level = 2, compact = compact) },
                heading3 = { model -> SpacedHeading(model, level = 3, compact = compact) },
                heading4 = { model -> SpacedHeading(model, level = 4, compact = compact) },
                paragraph = { model -> MarkdownParagraphComponent(model, imageResolver, directives, isDarkPage) },
                image = { model -> MarkdownImageComponent(model, imageResolver, directives, isDarkPage) },
                blockQuote = { model -> MarkdownCalloutComponent(model, isDarkPage) },
                codeBlock = { model -> HaetiCodeBlock(model, compact) },
                codeFence = { model -> HaetiCodeFence(model, compact) },
            ),
    )
}
