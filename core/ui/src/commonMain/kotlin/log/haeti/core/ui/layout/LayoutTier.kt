package log.haeti.core.ui.layout

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 글 본문 컬럼 최대 폭. 17sp 본문 기준 한 줄 45자 안팎의 읽기 폭이다. */
val ContentMaxWidth = 760.dp

/** 홈·목록 화면 컬럼 최대 폭. 썸네일이 붙는 만큼 본문보다 넓다. */
val HomeMaxWidth = 880.dp

private val CompactBreakpoint = 600.dp
private val SingleBreakpoint = 840.dp
private val DesktopBreakpoint = 1200.dp

/**
 * 반응형 레이아웃 구간. 뷰포트 폭을 600/840/1200dp 기준의 4단계로 나눈다.
 * 뷰포트 폭 측정은 앱 셸의 `BoxWithConstraints` 한 곳에서만 수행하고, 계산된 tier를
 * [LocalLayoutTier]로 내려보낸다. 하위 화면·컴포넌트는 그 값을 읽기만 한다.
 */
enum class LayoutTier {
    /** 600dp 미만. */
    Compact,

    /** 600dp 이상 840dp 미만. */
    Single,

    /** 840dp 이상 1200dp 미만. */
    Desktop,

    /** 1200dp 이상. */
    Wide,
}

/**
 * 현재 컴포지션의 반응형 구간. 앱 셸이 뷰포트 폭으로 계산해 제공하며,
 * 제공되지 않은 경우(프리뷰·테스트) [LayoutTier.Desktop]을 기본값으로 쓴다.
 */
val LocalLayoutTier: ProvidableCompositionLocal<LayoutTier> =
    staticCompositionLocalOf { LayoutTier.Desktop }

/** 뷰포트 폭으로 [LayoutTier]를 계산한다. */
fun layoutTierOf(width: Dp): LayoutTier =
    when {
        width < CompactBreakpoint -> LayoutTier.Compact
        width < SingleBreakpoint -> LayoutTier.Single
        width < DesktopBreakpoint -> LayoutTier.Desktop
        else -> LayoutTier.Wide
    }

/** 좌우 콘텐츠 패딩. Compact만 20dp, 나머지는 24dp. */
val LayoutTier.horizontalPadding: Dp
    get() = if (this == LayoutTier.Compact) 20.dp else 24.dp

/** 상단바 아래 콘텐츠 상단 여백. Desktop 이상 80dp, 그 미만 40dp. */
val LayoutTier.topSpacing: Dp
    get() = if (this == LayoutTier.Desktop || this == LayoutTier.Wide) 80.dp else 40.dp

/** 마크다운 본문·글 제목을 compact 스타일로 줄일지 여부. 600dp 미만에서만 true. */
val LayoutTier.isCompact: Boolean
    get() = this == LayoutTier.Compact

/** 상단바·푸터 정렬선 폭. 워드마크가 콘텐츠 시작선과 맞도록 홈 컬럼 폭과 같게 둔다. */
val LayoutTier.chromeMaxWidth: Dp
    get() = HomeMaxWidth
