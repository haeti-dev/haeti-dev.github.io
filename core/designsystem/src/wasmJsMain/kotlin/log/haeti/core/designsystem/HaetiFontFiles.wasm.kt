package log.haeti.core.designsystem

import log.haeti.core.designsystem.generated.resources.Res
import log.haeti.core.designsystem.generated.resources.jetbrains_mono_regular_web
import log.haeti.core.designsystem.generated.resources.pretendard_bold_web
import log.haeti.core.designsystem.generated.resources.pretendard_regular_web
import org.jetbrains.compose.resources.FontResource

/**
 * 웹은 공통 TTF를 WOFF2로 바꾼 파일을 쓴다. 글자는 같고 전송량이 약 30% 작다.
 * 만드는 법: `python3 -m fontTools.ttLib.woff2 compress -o <이름>_web.woff2 <이름>.ttf`.
 * JetBrains Mono는 먼저 `fontTools.subset --unicodes='*' --no-hinting`으로 hinting을 뺀다.
 */
internal actual object HaetiFontFiles {
    actual val regular: FontResource = Res.font.pretendard_regular_web
    actual val bold: FontResource = Res.font.pretendard_bold_web
    actual val mono: FontResource = Res.font.jetbrains_mono_regular_web
}
