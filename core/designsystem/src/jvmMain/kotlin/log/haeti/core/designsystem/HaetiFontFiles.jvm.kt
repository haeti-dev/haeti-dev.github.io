package log.haeti.core.designsystem

import log.haeti.core.designsystem.generated.resources.Res
import log.haeti.core.designsystem.generated.resources.jetbrains_mono_regular
import log.haeti.core.designsystem.generated.resources.pretendard_bold
import log.haeti.core.designsystem.generated.resources.pretendard_regular
import org.jetbrains.compose.resources.FontResource

/** 데스크톱은 공통 TTF를 그대로 쓴다. */
internal actual object HaetiFontFiles {
    actual val regular: FontResource = Res.font.pretendard_regular
    actual val bold: FontResource = Res.font.pretendard_bold
    actual val mono: FontResource = Res.font.jetbrains_mono_regular
}
