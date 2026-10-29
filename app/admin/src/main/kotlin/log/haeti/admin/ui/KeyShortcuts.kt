package log.haeti.admin.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/** macOS Cmd+S(저장) 눌림 여부를 판별한다. */
fun KeyEvent.isSaveShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.S && isMetaPressed

/** macOS Cmd+V(붙여넣기) 눌림 여부를 판별한다. */
fun KeyEvent.isPasteShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.V && isMetaPressed
