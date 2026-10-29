package log.haeti.admin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import log.haeti.admin.io.peekFrontmatter
import log.haeti.admin.model.PostEntry
import log.haeti.core.common.text.keepAll
import log.haeti.core.data.parser.PostParser
import log.haeti.core.designsystem.component.CategoryChip
import log.haeti.core.designsystem.haetiMonoFontFamily
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.markdown.MarkdownBody
import log.haeti.core.model.Language
import log.haeti.core.model.PostMeta
import log.haeti.core.model.dotDate
import log.haeti.core.model.readingTimeLabel
import log.haeti.core.ui.component.PostCover
import log.haeti.core.ui.component.PostMetaLine
import log.haeti.core.ui.platform.LocalPostAssetLoader
import log.haeti.core.ui.platform.PostAssetLoader
import java.nio.file.Files

/** 한국어 원문에 반드시 있어야 하는 프론트매터 키. 번역본은 제목·요약만 있으면 된다. */
private val RequiredKeys = listOf("title", "date", "category", "summary")

/**
 * 블로그 글 상세와 같은 순서(카테고리 → 제목 → 요약 → 날짜·분량 → 커버 → 본문)로 원문을 그린다.
 * 프론트매터가 덜 채워졌으면 무엇이 빠졌는지 위에 알린다.
 *
 * @param rawText 디바운스된 원문
 * @param entry 열려 있는 글 (번역본은 날짜·카테고리·썸네일을 여기서 가져온다)
 * @param language 편집 중인 파일 언어
 * @param imageResolver 본문 이미지 resolver
 * @param compact 모바일 폭이면 true
 */
@Composable
fun PreviewArticle(
    rawText: String,
    entry: PostEntry,
    language: Language,
    imageResolver: ImageResolver?,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val fields = remember(rawText) { peekFrontmatter(rawText) }
    val body = remember(rawText) { bodyOf(rawText) }
    val meta = remember(fields, entry, body) { previewMeta(fields, entry, body) }
    val missing = if (language == Language.KO) RequiredKeys.filter { fields[it].isNullOrBlank() } else emptyList()
    val loader = remember(entry.path) { diskAssetLoader(entry) }
    CompositionLocalProvider(LocalPostAssetLoader provides loader) {
        Column(modifier) {
            if (missing.isNotEmpty()) {
                WarningBanner(message = "프론트매터에 ${missing.joinToString(", ")}을(를) 채워 주세요")
                Spacer(Modifier.height(24.dp))
            }
            PreviewHeading(meta = meta, language = language, compact = compact)
            Spacer(Modifier.height(28.dp))
            PostCover(
                meta = meta,
                modifier = Modifier.fillMaxWidth().aspectRatio(if (compact) 1.6f else 2.2f),
                cornerRadius = 18.dp,
            )
            Spacer(Modifier.height(32.dp))
            if (imageResolver != null) {
                MarkdownBody(
                    markdown = body,
                    imageResolver = imageResolver,
                    modifier = Modifier.fillMaxWidth(),
                    compact = compact,
                    codeFontFamily = haetiMonoFontFamily(),
                )
            }
        }
    }
}

/** 글 머리. 카테고리, 제목(비어 있으면 흐린 "제목 없음"), 요약, 날짜·분량. */
@Composable
private fun PreviewHeading(
    meta: PostMeta,
    language: Language,
    compact: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    if (meta.category.isNotBlank()) CategoryChip(label = meta.category)
    Spacer(Modifier.height(14.dp))
    val titleStyle = MaterialTheme.typography.displaySmall
    val titleSize =
        if (compact) {
            titleStyle.copy(fontSize = 28.sp, lineHeight = 37.sp)
        } else {
            titleStyle.copy(fontSize = 36.sp, lineHeight = 46.sp)
        }
    Text(
        text = meta.title.ifBlank { if (language == Language.KO) "제목 없음" else "Untitled" }.keepAll(),
        style = titleSize,
        color = if (meta.title.isBlank()) colors.outlineVariant else colors.onBackground,
    )
    Spacer(Modifier.height(12.dp))
    Text(meta.summary.keepAll(), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
    Spacer(Modifier.height(18.dp))
    PostMetaLine(date = meta.dotDate(), readingTime = meta.readingTimeLabel())
}

/** 프론트매터와 목록 항목을 합쳐 미리보기용 [PostMeta]를 만든다. 날짜가 깨졌으면 목록 항목 날짜를 쓴다. */
private fun previewMeta(
    fields: Map<String, String>,
    entry: PostEntry,
    body: String,
): PostMeta {
    val date = fields["date"] ?: entry.date
    return PostMeta(
        title = fields["title"].orEmpty(),
        date = parseDateOr(date, parseDateOr(entry.date, LocalDate(2026, 1, 1))),
        category = fields["category"]?.takeIf { it.isNotBlank() } ?: entry.category,
        summary = fields["summary"].orEmpty(),
        slug = entry.slug,
        thumbnail = fields["thumbnail"]?.takeIf { it.isNotBlank() },
        readingMinutes = PostParser.readingMinutesOf(body),
    )
}

/** 날짜를 파싱하고, 형식이 깨졌으면 [fallback]. */
private fun parseDateOr(
    value: String,
    fallback: LocalDate,
): LocalDate = runCatching { LocalDate.parse(value) }.getOrDefault(fallback)

/** 프론트매터를 뗀 본문. 펜스가 없으면 원문 전체. */
private fun bodyOf(raw: String): String {
    val lines = raw.lines()
    if (lines.firstOrNull()?.trim() != "---") return raw
    val closing = lines.drop(1).indexOfFirst { it.trim() == "---" }
    return if (closing < 0) raw else lines.drop(closing + 2).joinToString("\n").trimStart('\n')
}

/** 썸네일 같은 글에 포함된 파일을 디스크에서 바로 읽는 로더. */
private fun diskAssetLoader(entry: PostEntry) =
    PostAssetLoader { _, relativePath ->
        withContext(Dispatchers.IO) {
            runCatching { Files.readAllBytes(entry.path.resolve(relativePath)) }.getOrNull()
        }
    }
