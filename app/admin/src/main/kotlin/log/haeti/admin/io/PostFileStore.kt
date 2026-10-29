package log.haeti.admin.io

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import log.haeti.admin.model.PostEntry
import log.haeti.admin.model.TranslationStatus
import log.haeti.core.model.Language
import java.nio.file.Files
import java.nio.file.Path

/** `yyyy-MM-dd-slug` 형식의 글 디렉터리명 검증 정규식. */
private val DIR_NAME_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}-[a-z0-9-]+$")

private const val INDEX_FILE_NAME = "index.md"
private const val IMAGES_DIR_NAME = "images"
private const val DATE_PREFIX_LENGTH = 10

/**
 * `content/posts/` 아래 글 디렉터리를 다루는 파일 IO 담당.
 * architecture.md 규칙대로 모든 읽기/쓰기가 이 클래스를 거친다.
 *
 * core:data의 `PostRepository`(발행된 글을 읽는 런타임 Repository)와 역할이 달라
 * 이름을 FileStore로 구분한다. 모든 메서드는 블로킹이므로 호출부가 IO 디스패처로 감싼다.
 *
 * @param contentRoot [locateContentRoot]로 찾은 content 디렉터리
 */
@Inject
@SingleIn(AppScope::class)
class PostFileStore(
    private val contentRoot: ContentRoot,
) {
    private val postsRoot: Path get() = contentRoot.path.resolve("posts")

    /**
     * `content/posts/` 하위 디렉터리를 디렉터리명 역순(최신순)으로 나열하고 프론트매터만 읽어 채운다.
     * 숨김 디렉터리(`.omc` 같은 도구 상태)는 글이 아니므로 건너뛴다.
     */
    fun listPosts(): List<PostEntry> {
        if (!Files.isDirectory(postsRoot)) return emptyList()
        return Files.newDirectoryStream(postsRoot).use { stream ->
            stream
                .filter { Files.isDirectory(it) && !it.fileName.toString().startsWith(".") }
                .map(::entryOf)
                .sortedByDescending { it.dirName }
        }
    }

    /** 글 원문을 읽는다. 한국어는 `index.md`, 다른 언어는 `index.<code>.md`. 파일이 없으면 null. */
    fun readPost(
        postDir: Path,
        language: Language,
    ): String? = postDir.resolve(fileNameOf(language)).takeIf { Files.isRegularFile(it) }?.let(Files::readString)

    /** 글 원문을 그대로 덮어쓴다. 번역본은 처음 저장할 때 파일이 생긴다. 파싱 성패와 무관하게 원문을 보존한다. */
    fun writePost(
        postDir: Path,
        language: Language,
        content: String,
    ) {
        Files.writeString(postDir.resolve(fileNameOf(language)), content)
    }

    /** 언어별 원문 파일명. */
    fun fileNameOf(language: Language): String =
        if (language ==
            Language.KO
        ) {
            INDEX_FILE_NAME
        } else {
            "index.${language.code}.md"
        }

    /** 번역본이 있는지, 있으면 지금 원문을 기준으로 만들어졌는지 판정한다. */
    private fun translationStatusOf(
        postDir: Path,
        korean: String?,
    ): TranslationStatus {
        val english = runCatching { readPost(postDir, Language.EN) }.getOrNull() ?: return TranslationStatus.Missing
        val recorded = peekFrontmatter(english)[SOURCE_KEY]
        val current = korean?.let(::translationSourceHash)
        return if (recorded != null && recorded == current) TranslationStatus.UpToDate else TranslationStatus.Stale
    }

    /** 디렉터리 하나를 [PostEntry]로 만든다. 원문이 없거나 깨져도 디렉터리명만으로 항목을 만든다. */
    private fun entryOf(postDir: Path): PostEntry {
        val dirName = postDir.fileName.toString()
        val korean = runCatching { readPost(postDir, Language.KO) }.getOrNull()
        val fields = korean?.let(::peekFrontmatter).orEmpty()
        return PostEntry(
            dirName = dirName,
            path = postDir,
            title = fields["title"].orEmpty(),
            date = fields["date"]?.takeIf { it.isNotBlank() } ?: dirName.take(DATE_PREFIX_LENGTH),
            category = fields["category"].orEmpty(),
            draft = fields["draft"]?.toBooleanStrictOrNull() ?: false,
            translation = translationStatusOf(postDir, korean),
        )
    }

    /**
     * 새 글 디렉터리와 `images/`를 만들고 프론트매터 템플릿을 담은 `index.md`를 생성한다.
     *
     * @param dirName `yyyy-MM-dd-slug` 형식 이름
     * @param title 프론트매터에 미리 채울 제목
     * @throws IllegalArgumentException 이름 형식이 잘못됐거나 이미 존재할 때
     */
    fun createPost(
        dirName: String,
        title: String = "",
    ): PostEntry {
        require(DIR_NAME_PATTERN.matches(dirName)) { "yyyy-MM-dd-slug 형식이어야 합니다: $dirName" }
        val postDir = postsRoot.resolve(dirName)
        require(!Files.exists(postDir)) { "이미 존재하는 디렉터리입니다: $dirName" }
        Files.createDirectories(postDir.resolve(IMAGES_DIR_NAME))
        Files.writeString(postDir.resolve(INDEX_FILE_NAME), templateFor(dirName, title))
        return entryOf(postDir)
    }

    /** 이미지를 글의 `images/`에 저장하고, 실제 사용된 파일명을 반환한다. 이름이 겹치면 `-1`, `-2`를 붙인다. */
    fun saveImage(
        postDir: Path,
        image: ImageFile,
    ): String {
        val imagesDir = postDir.resolve(IMAGES_DIR_NAME)
        Files.createDirectories(imagesDir)
        val fileName = uniqueFileName(imagesDir, "${image.baseName}.${image.extension}")
        Files.write(imagesDir.resolve(fileName), image.bytes)
        return fileName
    }

    private fun uniqueFileName(
        imagesDir: Path,
        original: String,
    ): String {
        if (!Files.exists(imagesDir.resolve(original))) return original
        val dot = original.lastIndexOf('.')
        val base = if (dot >= 0) original.substring(0, dot) else original
        val ext = if (dot >= 0) original.substring(dot) else ""
        var index = 1
        var candidate: String
        do {
            candidate = "$base-$index$ext"
            index++
        } while (Files.exists(imagesDir.resolve(candidate)))
        return candidate
    }

    private fun templateFor(
        dirName: String,
        title: String,
    ): String {
        val date = dirName.take(DATE_PREFIX_LENGTH)
        return """
            |---
            |title: $title
            |date: $date
            |category:
            |summary:
            |draft: true
            |---
            |
            """.trimMargin()
    }
}
