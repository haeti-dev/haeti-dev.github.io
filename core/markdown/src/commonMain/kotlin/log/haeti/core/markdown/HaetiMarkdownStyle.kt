package log.haeti.core.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography

private val INLINE_CODE_BACKGROUND_LIGHT = Color(0xFFEEF2FC)
private val INLINE_CODE_TEXT_LIGHT = Color(0xFF2257C9)
private val INLINE_CODE_BACKGROUND_DARK = Color(0xFF1C2638)
private val INLINE_CODE_TEXT_DARK = Color(0xFF8DB6FF)

/**
 * 코드블록 배경은 [HAETI_CODE_CARD_BACKGROUND]로 고정하고, 인라인 코드만 [isDark]에 따라
 * 노션 스타일 라이트/다크 팔레트를 고른다. 나머지 색(본문·구분선·표 등)은 라이브러리 기본값
 * ([markdownColor]가 [androidx.compose.material3.MaterialTheme]에서 파생).
 *
 * @param isDark `MaterialTheme.colorScheme.background`의 밝기로 판정한 다크모드 여부
 */
@Composable
internal fun haetiMarkdownColors(isDark: Boolean): MarkdownColors =
    markdownColor(
        codeBackground = HAETI_CODE_CARD_BACKGROUND,
        inlineCodeBackground = if (isDark) INLINE_CODE_BACKGROUND_DARK else INLINE_CODE_BACKGROUND_LIGHT,
    )

/**
 * 마크다운 타이포 스케일. 렌더러 기본값은 h1~h3에 displayLarge/Medium/Small
 * (57/45/36sp)을 써서 글 제목(36sp)보다 커지므로, 헤딩·본문·리스트·인라인코드 크기를 전부
 * 이 함수에서 명시적으로 고정한다. 폰트 패밀리(Pretendard)만 [MaterialTheme.typography]에서
 * 상속하고 크기·굵기는 상속하지 않는다.
 *
 * @param isDark 인라인 코드 팔레트 선택용 다크모드 여부
 * @param compact 좁은 화면(<600dp)용 축소 스케일 적용 여부
 */
@Composable
internal fun haetiMarkdownTypography(
    isDark: Boolean,
    compact: Boolean,
): MarkdownTypography {
    val headingFont = MaterialTheme.typography.headlineSmall.fontFamily
    val bodyFont = MaterialTheme.typography.bodyLarge.fontFamily
    val bodyStyle =
        TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = if (compact) 16.sp else 17.sp,
            lineHeight = if (compact) 27.sp else 29.sp,
        )
    return markdownTypography(
        h1 = headingStyle(headingFont, if (compact) 26.sp else 30.sp, if (compact) 35.sp else 40.sp),
        h2 = headingStyle(headingFont, if (compact) 22.sp else 26.sp, if (compact) 30.sp else 36.sp),
        h3 = headingStyle(headingFont, if (compact) 19.sp else 21.sp, if (compact) 27.sp else 30.sp),
        h4 = headingStyle(headingFont, 18.sp, 26.sp),
        text = bodyStyle,
        paragraph = bodyStyle,
        bullet = bodyStyle,
        list = bodyStyle,
        inlineCode =
            TextStyle(
                fontFamily = LocalCodeFontFamily.current,
                fontSize = if (compact) 13.sp else 14.sp,
                color = if (isDark) INLINE_CODE_TEXT_DARK else INLINE_CODE_TEXT_LIGHT,
            ),
    )
}

/** 헤딩용 [TextStyle]을 만든다. 헤딩은 전부 굵게(700)이므로 크기·행간만 인자로 받는다. */
private fun headingStyle(
    family: FontFamily?,
    size: TextUnit,
    lineHeight: TextUnit,
) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Bold,
    fontSize = size,
    lineHeight = lineHeight,
)
