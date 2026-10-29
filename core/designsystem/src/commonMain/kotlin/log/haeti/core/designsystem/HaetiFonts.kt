package log.haeti.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import log.haeti.core.designsystem.generated.resources.Res
import log.haeti.core.designsystem.generated.resources.jetbrains_mono_regular
import log.haeti.core.designsystem.generated.resources.pretendard_bold
import log.haeti.core.designsystem.generated.resources.pretendard_regular
import org.jetbrains.compose.resources.Font

/**
 * 본문·제목용 Pretendard 폰트 패밀리. 400/700 두 굵기만 번들한다.
 */
@Composable
fun haetiPretendardFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.pretendard_regular, FontWeight.Normal),
        Font(Res.font.pretendard_bold, FontWeight.Bold),
    )

/**
 * 코드용 JetBrains Mono 폰트 패밀리. v3부터 메타(날짜)는 본문 폰트를 쓰므로 코드 전용이다.
 * 400 하나만 번들한다.
 */
@Composable
fun haetiMonoFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
    )
