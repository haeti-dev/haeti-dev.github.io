package log.haeti.core.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans
import com.mikepenz.markdown.compose.extendedspans.RoundedCornerSpanPainter
import com.mikepenz.markdown.model.MarkdownExtendedSpans
import com.mikepenz.markdown.model.markdownExtendedSpans

/** 라인 박스 위·아래에서 줄일 양(sp). 본문(0)과 헤딩 레벨(1~4)별, 넓은 화면/좁은 화면 순. */
private val chipMargins: Map<Int, Pair<Pair<Float, Float>, Pair<Float, Float>>> =
    mapOf(
        0 to ((3.5f to 2f) to (4f to 1.5f)),
        1 to ((16f to 5f) to (14f to 4f)),
        2 to ((13f to 3.5f) to (9.5f to 3f)),
        3 to ((8f to 2.5f) to (7f to 2f)),
        4 to ((4.5f to 2f) to (5.5f to 2.5f)),
    )

/**
 * 인라인 코드 배경을 위아래 여백이 같은 둥근 칩으로 그린다.
 *
 * 렌더러 기본은 글자 배경색이라 높이를 코드 폰트 수치가 정해 아래로 처진다. 대신 라인 박스에서 위아래를
 * 줄여 칩을 직접 그린다. 줄이는 양은 줄 높이마다 달라 본문과 헤딩 레벨별로 2배 밀도 렌더를 재서 맞췄다
 * (코드 글자 위아래 2.5dp). 타이포([haetiMarkdownTypography])의 줄 높이를 바꾸면 다시 잰다.
 *
 * @param compact 좁은 화면용 타이포를 쓰는지
 * @param headingLevel 헤딩 안이면 레벨(1~4), 본문이면 0
 */
@Composable
internal fun rememberInlineCodeChips(
    compact: Boolean,
    headingLevel: Int = 0,
): MarkdownExtendedSpans =
    markdownExtendedSpans {
        remember(compact, headingLevel) {
            val (wide, narrow) = chipMargins[headingLevel] ?: chipMargins.getValue(0)
            val (top, bottom) = if (compact) narrow else wide
            ExtendedSpans(
                RoundedCornerSpanPainter(
                    cornerRadius = 5.sp,
                    // 렌더러가 코드 앞뒤에 붙이는 공백만큼 칩이 넓어지므로 양옆을 조금 줄인다.
                    padding = RoundedCornerSpanPainter.TextPaddingValues(horizontal = (-4).sp, vertical = 2.sp),
                    topMargin = top.sp,
                    bottomMargin = bottom.sp,
                ),
            )
        }
    }
