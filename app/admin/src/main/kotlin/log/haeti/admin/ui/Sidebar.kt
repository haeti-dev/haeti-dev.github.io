package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import log.haeti.admin.ui.component.AdminButton
import log.haeti.admin.ui.component.AdminButtonStyle
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.ui.component.HaetiWordmark

/**
 * 왼쪽 사이드바. 워드마크·테마 전환, 새 글 버튼, 글 목록(최신순)을 쌓는다.
 *
 * @param state 화면 상태
 * @param actions 사용자 액션
 * @param onNewPost 새 글 버튼 클릭 시 호출
 */
@Composable
fun Sidebar(
    state: AdminUiState,
    actions: AdminActions,
    onNewPost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        SidebarHeader(isDarkTheme = state.isDarkTheme, onToggleTheme = actions.onToggleTheme)
        AdminButton(
            text = "새 글",
            onClick = onNewPost,
            style = AdminButtonStyle.Primary,
            icon = Icons.Outlined.Add,
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        )
        PostCountHeading(count = state.posts.size)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(state.posts, key = { it.dirName }) { entry ->
                PostListItem(
                    entry = entry,
                    isSelected = entry.dirName == state.selected?.dirName,
                    isTranslating = entry.dirName in state.translating,
                    onClick = { actions.onSelect(entry) },
                    onTranslate = { actions.onTranslate(entry) },
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
            }
        }
        state.notice?.let { notice ->
            NoticeBanner(
                message = notice.message,
                failed = notice.failed,
                onDismiss = actions.onDismissNotice,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

/** 글 목록 위의 "글 N" 제목. */
@Composable
private fun PostCountHeading(count: Int) {
    Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)) {
        Text("글", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

/** 워드마크 + "Admin" 표기, 오른쪽 끝 테마 전환 버튼. */
@Composable
private fun SidebarHeader(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HaetiWordmark()
        Spacer(Modifier.width(8.dp))
        Text("Admin", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            contentDescription = "다크 모드 켜기/끄기",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .haetiClickable(onClick = onToggleTheme)
                    .padding(8.dp)
                    .size(18.dp),
        )
    }
}
