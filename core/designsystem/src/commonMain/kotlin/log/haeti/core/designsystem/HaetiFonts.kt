package log.haeti.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font

/**
 * 본문·제목용 Pretendard 폰트 패밀리. 400/700 두 굵기만 번들한다.
 */
@Composable
fun haetiPretendardFontFamily(): FontFamily =
    FontFamily(
        Font(HaetiFontFiles.regular, FontWeight.Normal),
        Font(HaetiFontFiles.bold, FontWeight.Bold),
    )

/**
 * 코드용 JetBrains Mono 폰트 패밀리. v3부터 메타(날짜)는 본문 폰트를 쓰므로 코드 전용이다.
 * 400 하나만 번들한다.
 */
@Composable
fun haetiMonoFontFamily(): FontFamily =
    FontFamily(
        Font(HaetiFontFiles.mono, FontWeight.Normal),
    )
