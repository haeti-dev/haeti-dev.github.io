package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import log.haeti.admin.AdminViewModel
import log.haeti.admin.insertImages

private val SidebarWidth = 300.dp

/**
 * ViewModel을 화면 상태로 풀어 [AdminContent]에 넘기는 진입 컴포저블.
 *
 * @param viewModel 어드민 ViewModel
 * @param isDarkTheme 다크 테마 여부
 * @param onToggleTheme 테마 전환 콜백
 */
@Composable
fun AdminApp(
    viewModel: AdminViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state =
        AdminUiState(
            posts = viewModel.posts,
            selected = viewModel.selected,
            language = viewModel.language,
            fileName = viewModel.fileName,
            fileExists = viewModel.fileExists,
            editorValue = viewModel.editorValue,
            isDirty = viewModel.isDirty,
            imageResolver = viewModel.imageResolver,
            isDarkTheme = isDarkTheme,
            translating = viewModel.translations.running,
            notice = viewModel.fileNotice ?: viewModel.translations.notice,
            isEditorLocked = viewModel.isEditorLocked,
        )
    val actions =
        AdminActions(
            onSelect = viewModel::open,
            onCreate = viewModel::createPost,
            onSelectLanguage = viewModel::selectLanguage,
            onEditorChange = viewModel::updateEditor,
            onSave = viewModel::save,
            onInsertImages = viewModel::insertImages,
            onTranslate = viewModel::translate,
            onDismissNotice = viewModel::dismissNotice,
            onToggleTheme = onToggleTheme,
        )
    AdminContent(state = state, actions = actions, modifier = modifier)
}

/**
 * 어드민 화면 배치. 왼쪽 글 목록, 오른쪽은 헤더 아래 에디터와 미리보기 2분할이다.
 *
 * @param state 화면 상태
 * @param actions 사용자 액션
 * @param initialDialog 새 글 다이얼로그를 연 채로 시작할지 (스크린샷 렌더용)
 */
@Composable
fun AdminContent(
    state: AdminUiState,
    actions: AdminActions,
    modifier: Modifier = Modifier,
    initialDialog: Boolean = false,
) {
    var showNewPost by remember { mutableStateOf(initialDialog) }
    Box(modifier.fillMaxSize()) {
        Workspace(state, actions, onNewPost = { showNewPost = true })
        if (showNewPost) NewPostDialog(onCreate = actions.onCreate, onDismiss = { showNewPost = false })
    }
}

/** 사이드바 + 작업 영역. */
@Composable
private fun Workspace(
    state: AdminUiState,
    actions: AdminActions,
    onNewPost: () -> Unit,
) {
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Sidebar(state, actions, onNewPost, Modifier.width(SidebarWidth).fillMaxHeight())
        VerticalLine()
        if (state.selected == null) {
            EmptyWorkspace(postCount = state.posts.size, modifier = Modifier.weight(1f).fillMaxHeight())
            return@Row
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            EditorHeader(state = state, actions = actions)
            Row(Modifier.weight(1f).fillMaxSize()) {
                EditorPane(
                    value = state.editorValue,
                    onValueChange = actions.onEditorChange,
                    onDropImages = actions.onInsertImages,
                    readOnly = state.isEditorLocked,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                VerticalLine()
                PreviewPane(
                    rawText = state.editorValue.text,
                    entry = state.selected,
                    language = state.language,
                    imageResolver = state.imageResolver,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

/** 영역 사이 1dp 세로 구분선. */
@Composable
internal fun VerticalLine() {
    Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))
}
