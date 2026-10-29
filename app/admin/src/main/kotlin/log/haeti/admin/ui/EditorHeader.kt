package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import log.haeti.admin.io.IncomingImages
import log.haeti.admin.io.pickImageFile
import log.haeti.admin.model.PostEntry
import log.haeti.admin.model.TranslationStatus
import log.haeti.admin.ui.component.AdminButton
import log.haeti.admin.ui.component.AdminButtonStyle
import log.haeti.core.designsystem.haetiMonoFontFamily
import log.haeti.core.model.Language
import log.haeti.core.ui.component.LanguageToggle

private val SavedColor = Color(0xFF138A5E)
private val UnsavedColor = Color(0xFFB97709)

/**
 * 에디터·미리보기 위 헤더. 왼쪽은 편집 중인 파일 경로와 언어 탭, 오른쪽은 저장 상태와 액션 버튼.
 *
 * @param state 화면 상태
 * @param actions 사용자 액션
 */
@Composable
fun EditorHeader(
    state: AdminUiState,
    actions: AdminActions,
    modifier: Modifier = Modifier,
) {
    val entry = state.selected ?: return
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilePath(directory = entry.dirName, fileName = state.fileName, modifier = Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(16.dp))
            LanguageToggle(language = state.language, onSelect = actions.onSelectLanguage)
            Spacer(Modifier.weight(1f))
            SaveStatus(
                isDirty = state.isDirty,
                fileExists = state.fileExists,
                stale = state.language == Language.EN && entry.translation == TranslationStatus.Stale,
            )
            Spacer(Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TranslateButton(
                    entry,
                    isRunning = entry.dirName in state.translating,
                    onTranslate = actions.onTranslate,
                )
                AdminButton(
                    text = "이미지 넣기",
                    onClick = { pickImageFile()?.let { actions.onInsertImages(IncomingImages.Files(listOf(it))) } },
                    icon = Icons.Outlined.Image,
                )
                AdminButton(
                    text = "저장",
                    onClick = actions.onSave,
                    style = AdminButtonStyle.Primary,
                    hint = "⌘S",
                    enabled = state.isDirty,
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
    }
}

/** 번역본이 없거나 낡았을 때만 보이는 번역 버튼. 도는 동안은 눌리지 않는다. */
@Composable
private fun TranslateButton(
    entry: PostEntry,
    isRunning: Boolean,
    onTranslate: (PostEntry) -> Unit,
) {
    val label =
        when {
            isRunning -> "번역 중…"
            entry.translation == TranslationStatus.Missing -> "번역 만들기"
            entry.translation == TranslationStatus.Stale -> "번역 갱신"
            else -> return
        }
    AdminButton(text = label, onClick = { onTranslate(entry) }, icon = Icons.Outlined.Translate, enabled = !isRunning)
}

/** `posts / <디렉터리> / index.md` 경로. 파일명만 진하게. */
@Composable
private fun FilePath(
    directory: String,
    fileName: String,
    modifier: Modifier = Modifier,
) {
    val muted = MaterialTheme.colorScheme.outlineVariant
    val style = MaterialTheme.typography.labelMedium.copy(fontFamily = haetiMonoFontFamily())
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "posts / $directory / ",
            style = style,
            color = muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text = fileName,
            style = style.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** 저장 상태 점 + 문구. 저장됨(초록)·저장 안 됨(주황)·원문 변경(주황)·아직 없는 번역본(브랜드). */
@Composable
private fun SaveStatus(
    isDirty: Boolean,
    fileExists: Boolean,
    stale: Boolean,
) {
    val (color, label) =
        when {
            isDirty -> UnsavedColor to "저장 안 됨"
            stale -> UnsavedColor to "원문이 바뀌어 번역 갱신이 필요합니다"
            !fileExists -> MaterialTheme.colorScheme.primary to "저장하면 번역본이 생깁니다"
            else -> SavedColor to "저장됨"
        }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
