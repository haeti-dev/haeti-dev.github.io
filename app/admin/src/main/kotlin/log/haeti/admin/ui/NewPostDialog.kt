package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import log.haeti.admin.ui.component.AdminButton
import log.haeti.admin.ui.component.AdminButtonStyle
import log.haeti.admin.ui.component.AdminTextField
import log.haeti.core.designsystem.haetiMonoFontFamily
import java.time.LocalDate

private val DATE_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}$")
private val SLUG_PATTERN = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")

/**
 * 새 글 만들기 오버레이. 제목·날짜·슬러그를 받고, 만들어질 폴더 경로를 미리 보여준다.
 * 창 전체를 흐리게 덮고 가운데 카드를 띄운다. 바깥을 누르면 닫힌다.
 *
 * @param onCreate 디렉터리명과 제목으로 생성을 시도하고 성공 여부를 돌려준다
 * @param onDismiss 닫기
 */
@Composable
fun NewPostDialog(
    onCreate: (String, String) -> Boolean,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var slug by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }
    val dateValid = DATE_PATTERN.matches(date)
    val slugValid = SLUG_PATTERN.matches(slug)
    DialogCard(onDismiss = onDismiss) {
        Text("새 글", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        AdminTextField("제목", title, { title = it }, placeholder = "글 제목 (나중에 바꿀 수 있습니다)")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminTextField("날짜", date, {
                date = it
                failed = false
            }, Modifier.width(150.dp), isError = !dateValid)
            AdminTextField(
                label = "슬러그",
                value = slug,
                onValueChange = {
                    slug = it.lowercase()
                    failed = false
                },
                placeholder = "my-first-post",
                isError = slug.isNotEmpty() && !slugValid,
                modifier = Modifier.weight(1f),
            )
        }
        PathPreview(directory = "$date-${slug.ifBlank { "slug" }}", failed = failed)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            AdminButton(text = "취소", onClick = onDismiss)
            AdminButton(
                text = "만들기",
                onClick = { if (onCreate("$date-$slug", title.trim())) onDismiss() else failed = true },
                style = AdminButtonStyle.Primary,
                enabled = dateValid && slugValid,
            )
        }
    }
}

/** 창 전체를 흐리게 덮고 가운데 480dp 카드를 띄운다. 바깥을 누르면 [onDismiss], 카드 안의 클릭은 막는다. */
@Composable
private fun DialogCard(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .width(480.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

/** 만들어질 폴더 경로 또는 실패 이유. 슬러그는 영문 소문자·숫자·하이픈만 쓴다. */
@Composable
private fun PathPreview(
    directory: String,
    failed: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(10.dp)).padding(12.dp)) {
        Text(
            text = "content/posts/$directory/index.md",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = haetiMonoFontFamily()),
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (failed) "같은 이름의 글이 이미 있습니다. 슬러그를 바꿔 주세요." else "슬러그는 영문 소문자, 숫자, 하이픈으로 씁니다.",
            style = MaterialTheme.typography.labelMedium,
            color = if (failed) colors.error else colors.outlineVariant,
        )
    }
}
