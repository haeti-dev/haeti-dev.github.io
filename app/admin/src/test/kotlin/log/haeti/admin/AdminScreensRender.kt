package log.haeti.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import log.haeti.admin.image.AdminImageResolver
import log.haeti.admin.io.ContentRoot
import log.haeti.admin.io.PostFileStore
import log.haeti.admin.model.PostEntry
import log.haeti.admin.model.TranslationStatus
import log.haeti.admin.translate.TranslationNotice
import log.haeti.admin.ui.AdminActions
import log.haeti.admin.ui.AdminContent
import log.haeti.admin.ui.AdminUiState
import log.haeti.core.designsystem.HaetiTheme
import log.haeti.core.model.Language
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.test.Test

/**
 * 어드민 화면을 창 없이 PNG로 그린다. 디자인 검토용이라 `-Phaeti.renderScreens=true`일 때만 돈다.
 * 실제 글(`content/posts`)을 임시 폴더에 복사해 목록을 채운다. 원본은 건드리지 않는다.
 */
class AdminScreensRender {
    private val enabled = System.getProperty("haeti.renderScreens") == "true"
    private val outDir by lazy { File(System.getProperty("haeti.screensDir")).apply { mkdirs() } }

    @Test
    fun renderScreens() {
        if (!enabled) return
        val store = PostFileStore(ContentRoot(stagedContent()))
        val posts = store.listPosts()
        val latest = posts.first()
        val untranslated = posts.firstOrNull { it.translation == TranslationStatus.Missing } ?: latest
        render("1-editor-light", state(store, posts, latest, Language.KO, dark = false))
        render("2-editor-dark", state(store, posts, latest, Language.KO, dark = true))
        render("3-editor-en-new", state(store, posts, untranslated, Language.EN, dark = false))
        render("4-empty", state(store, posts, null, Language.KO, dark = false))
        render("5-new-post", state(store, posts, null, Language.KO, dark = false), dialog = true)
        val stale = posts.firstOrNull { it.translation == TranslationStatus.Stale } ?: latest
        val notice = TranslationNotice("‘${untranslated.title}’ 영어 번역본을 갱신했습니다. EN 탭에서 확인하세요.", false)
        val translating = state(store, posts, stale, Language.KO, dark = false)
        render("6-translate", translating.copy(translating = setOf(untranslated.dirName), notice = notice))
    }

    private fun state(
        store: PostFileStore,
        posts: List<PostEntry>,
        selected: PostEntry?,
        language: Language,
        dark: Boolean,
    ): AdminUiState {
        val text = selected?.let { store.readPost(it.path, language) }
        val editor = text ?: if (language == Language.EN) "---\ntitle: \nsummary: \n---\n\n" else ""
        return AdminUiState(
            posts = posts,
            selected = selected,
            language = language,
            fileName = store.fileNameOf(language),
            fileExists = text != null,
            editorValue = TextFieldValue(editor),
            isDirty = false,
            imageResolver = selected?.let { AdminImageResolver(it.path) },
            isDarkTheme = dark,
        )
    }

    private fun render(
        name: String,
        state: AdminUiState,
        dialog: Boolean = false,
    ) {
        val content: @Composable () -> Unit = {
            HaetiTheme(darkTheme = state.isDarkTheme) { AdminContent(state, AdminActions(), initialDialog = dialog) }
        }
        ImageComposeScene(width = 2880, height = 1800, density = Density(2f), content = content).use { scene ->
            var time = 0L
            repeat(12) {
                scene.render(time)
                time += 100_000_000L
                Thread.sleep(60)
            }
            val png = scene.render(time).encodeToData(EncodedImageFormat.PNG)!!.bytes
            File(outDir, "$name.png").writeBytes(png)
        }
    }

    /** content/posts를 임시 폴더로 복사한다. */
    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    private fun stagedContent(): Path {
        val source = Path.of(System.getProperty("haeti.contentRoot"))
        val root = Files.createTempDirectory("haeti-admin-screens")
        val posts = root.resolve("posts").createDirectories()
        listOf("posts").map(source::resolve).filter(Files::isDirectory).forEach { dir ->
            Files.list(dir).filter(Files::isDirectory).forEach {
                it.copyToRecursively(posts.resolve(it.fileName.toString()), followLinks = false)
            }
        }
        return root
    }
}
