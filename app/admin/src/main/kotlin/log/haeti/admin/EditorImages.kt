package log.haeti.admin

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import log.haeti.admin.editor.insertBlock
import log.haeti.admin.io.IncomingImages
import log.haeti.admin.io.importAsMarkdown

/**
 * 이미지를 다듬어 글의 `images/`에 넣고 커서 위치에 마크다운 조각을 삽입한다. 여러 장이면 빈 줄로 잇는다.
 * 번역 CLI가 쓰는 중인 번역본(잠금)에는 넣지 않는다. 넣으면 CLI의 결과를 낡은 버퍼로 덮어쓰게 된다.
 */
fun AdminViewModel.insertImages(incoming: IncomingImages) {
    if (isEditorLocked) return
    val entry = selected ?: return
    val target = language
    viewModelScope.launch {
        val snippet = withContext(Dispatchers.IO) { images.importAsMarkdown(incoming, entry.path) } ?: return@launch
        // 가져오는 사이 다른 글·언어로 옮겼으면 그 파일에 넣지 않는다.
        if (selected?.dirName == entry.dirName && language == target) updateEditor(editorValue.insertBlock(snippet))
    }
}
