package log.haeti.core.markdown

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/** AWT `Toolkit`의 시스템 클립보드로 복사한다 (admin 데스크톱 미리보기용). */
internal actual fun copyToClipboard(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
