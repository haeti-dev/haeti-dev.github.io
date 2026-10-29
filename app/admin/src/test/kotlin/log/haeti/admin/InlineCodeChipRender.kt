package log.haeti.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import log.haeti.core.designsystem.HaetiTheme
import log.haeti.core.designsystem.haetiMonoFontFamily
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.markdown.MarkdownBody
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test

/**
 * 인라인 코드 칩을 본문·목록·인용·헤딩 1~4 안에서 PNG로 그린다(2배 밀도, 넓은/좁은 화면 × 라이트/다크).
 * 마크다운 줄 높이를 바꾼 뒤 칩 위아래 여백(`InlineCodeChip.kt`)을 다시 잴 때 쓴다.
 * `./gradlew :app:admin:test -Phaeti.renderScreens=true`일 때만 돈다.
 */
class InlineCodeChipRender {
    @Test
    fun render() {
        if (System.getProperty("haeti.renderScreens") != "true") return
        val out = File(System.getProperty("haeti.screensDir")).apply { mkdirs() }
        val md =
            "각 화면에서는 `hiltViewModel()` 과 `NavDisplay` 를 함께 쓴다.\n\n" +
                "- 목록 안의 `keys` 와 `Any`\n\n> 인용 안의 `remember`\n\n# 제목1 `hiltVM()`\n\n## 제목2 `hiltVM()`\n\n### 제목3 `hiltVM()`\n\n#### 제목4 `hiltVM()`"
        for ((name, compact) in listOf("desktop" to false, "compact" to true)) {
            for (dark in listOf(false, true)) {
                ImageComposeScene(width = 1400, height = 1700, density = Density(2f)) {
                    HaetiTheme(darkTheme = dark) {
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
                            MarkdownBody(
                                markdown = md,
                                imageResolver = ImageResolver { null },
                                compact = compact,
                                codeFontFamily = haetiMonoFontFamily(),
                            )
                        }
                    }
                }.use { scene ->
                    var t = 0L
                    repeat(10) {
                        scene.render(t)
                        t += 100_000_000L
                        Thread.sleep(50)
                    }
                    val png = scene.render(t).encodeToData(EncodedImageFormat.PNG)!!.bytes
                    File(out, "code-$name-${if (dark) "dark" else "light"}.png").writeBytes(png)
                }
            }
        }
    }
}
