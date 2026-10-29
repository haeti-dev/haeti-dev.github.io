package log.haeti.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 블로그 타이포 스케일을 M3 [Typography] 슬롯에 매핑한다. 전 슬롯 Pretendard,
 * 굵기는 번들된 400/700 두 가지만 쓴다. 큰 글자일수록 자간을 좁혀 한글 제목이 퍼지지 않게 한다.
 *
 * | 슬롯 | 용도 |
 * |---|---|
 * | displaySmall | 글 상세 제목 |
 * | headlineLarge | 홈 소개 문장 |
 * | headlineMedium | 최신 글 제목, 마크다운 h1 |
 * | titleLarge | 목록 글 제목 |
 * | titleMedium | 섹션 제목 |
 * | bodyLarge | 본문 |
 * | bodyMedium | 요약 |
 * | labelLarge | 내비·링크 |
 * | labelMedium | 메타(날짜·분량) |
 *
 * @param pretendard 전 슬롯에 쓰는 본문 폰트. 코드용 모노 폰트는 [haetiMonoFontFamily]를 쓴다
 */
internal fun haetiTypography(pretendard: FontFamily): Typography =
    Typography(
        displaySmall = bold(pretendard, 44.sp, 56.sp, (-0.03).em),
        headlineLarge = bold(pretendard, 34.sp, 46.sp, (-0.025).em),
        headlineMedium = bold(pretendard, 28.sp, 38.sp, (-0.02).em),
        headlineSmall = bold(pretendard, 24.sp, 34.sp, (-0.015).em),
        titleLarge = bold(pretendard, 20.sp, 29.sp, (-0.01).em),
        titleMedium = bold(pretendard, 17.sp, 25.sp, 0.em),
        bodyLarge = regular(pretendard, 17.sp, 30.sp),
        bodyMedium = regular(pretendard, 15.sp, 25.sp),
        bodySmall = regular(pretendard, 13.sp, 20.sp),
        labelLarge = regular(pretendard, 15.sp, 22.sp),
        labelMedium = regular(pretendard, 14.sp, 20.sp),
        labelSmall = regular(pretendard, 12.sp, 16.sp),
    )

private fun bold(
    family: FontFamily,
    size: TextUnit,
    lineHeight: TextUnit,
    tracking: TextUnit,
) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Bold,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = tracking,
)

private fun regular(
    family: FontFamily,
    size: TextUnit,
    lineHeight: TextUnit,
) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Normal,
    fontSize = size,
    lineHeight = lineHeight,
)
