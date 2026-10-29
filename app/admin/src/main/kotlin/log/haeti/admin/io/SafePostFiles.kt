package log.haeti.admin.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import log.haeti.admin.translate.TranslationNotice
import log.haeti.core.model.Language
import java.nio.file.Path

/** 번역본 파일이 아직 없을 때 에디터에 채워 주는 시작점. */
private const val TRANSLATION_TEMPLATE = "---\ntitle: \nsummary: \n---\n\n"

/** 파일이 아직 없을 때 에디터에 채울 원문. 원문은 비우고, 번역본은 프론트매터 틀로 시작한다. */
internal fun emptyPostText(language: Language): String = if (language == Language.KO) "" else TRANSLATION_TEMPLATE

/**
 * 글 파일을 IO 스레드에서 읽는다. 파일이 없으면 성공(null)이고, 읽다가 실패하면(인코딩·권한 등) 실패다.
 * 둘을 구분해야 읽지 못한 원문을 빈 문서로 열었다가 저장해 덮어쓰는 일이 없다.
 */
internal suspend fun PostFileStore.readPostSafely(
    postDir: Path,
    language: Language,
): Result<String?> = withContext(Dispatchers.IO) { runCatching { readPost(postDir, language) } }

/** 글 파일을 IO 스레드에서 쓴다. 실패하면 화면에 띄울 안내를, 성공하면 null을 돌려준다. */
internal suspend fun PostFileStore.writePostSafely(
    postDir: Path,
    language: Language,
    content: String,
): TranslationNotice? =
    withContext(Dispatchers.IO) {
        runCatching { writePost(postDir, language, content) }
            .exceptionOrNull()
            ?.let { failureNotice("저장하지 못했습니다", fileNameOf(language), it) }
    }

/** 파일 읽기·쓰기 실패 안내. 원인 메시지를 함께 보여 준다. */
internal fun failureNotice(
    action: String,
    fileName: String,
    error: Throwable,
): TranslationNotice =
    TranslationNotice("$fileName $action. ${error.message ?: error::class.simpleName}", failed = true)

/** 이미지를 글의 `images/`에 넣고 본문에 넣을 마크다운 조각을 만든다. 여러 장이면 빈 줄로 잇고, 하나도 못 넣으면 null. */
internal fun ImageImporter.importAsMarkdown(
    incoming: IncomingImages,
    postDir: Path,
): String? =
    import(incoming, postDir)
        .takeIf { it.isNotEmpty() }
        ?.joinToString("\n\n") { "![](images/$it){width=100 align=center}" }
