package log.haeti.admin

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import log.haeti.admin.io.ContentRoot
import log.haeti.admin.io.ImageImporter
import log.haeti.admin.io.PostFileStore
import log.haeti.admin.translate.TranslationQueue
import log.haeti.admin.translate.TranslationRunner
import log.haeti.core.model.Language
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 글을 잃을 수 있는 경로(저장 실패, 읽기 실패, 빈 번역본 저장, 닫기)를 확인한다.
 * 파일 IO는 실제 스레드에서 돌기 때문에 [eventually]로 결과를 기다린다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {
    private val root = Files.createTempDirectory("admin-vm")
    private val store = PostFileStore(ContentRoot(root))
    private val first = store.createPost("2026-10-01-first", "첫 글")
    private val second = store.createPost("2026-10-02-second", "둘째 글")
    private lateinit var viewModel: AdminViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val translations = TranslationQueue(TranslationRunner(ContentRoot(root)), store)
        viewModel = AdminViewModel(store, ImageImporter(store), translations)
        eventually { viewModel.posts.size == 2 }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        root.toFile().deleteRecursively()
    }

    @Test
    fun `번역본 탭에서 아무것도 바꾸지 않고 저장하면 빈 번역본 파일을 만들지 않는다`() {
        openAndWait(first.dirName)
        viewModel.selectLanguage(Language.EN)
        eventually { viewModel.language == Language.EN }

        viewModel.save()

        assertFalse(Files.exists(first.path.resolve("index.en.md")))
    }

    @Test
    fun `저장에 실패하면 다른 글로 옮기지 않고 편집 내용과 안내를 남긴다`() {
        openAndWait(first.dirName)
        viewModel.updateEditor(TextFieldValue("고친 원문"))
        first.path.toFile().deleteRecursively()

        viewModel.open(second)
        eventually { viewModel.fileNotice != null }

        assertEquals(first.dirName, viewModel.selected?.dirName)
        assertEquals("고친 원문", viewModel.editorValue.text)
    }

    @Test
    fun `읽을 수 없는 원문은 빈 문서로 열지 않고 안내를 띄운다`() {
        Files.write(second.path.resolve("index.md"), byteArrayOf(0xC3.toByte(), 0x28))

        viewModel.open(second)
        eventually { viewModel.fileNotice != null }

        assertEquals(null, viewModel.selected)
        assertEquals(true, viewModel.fileNotice?.failed)
    }

    @Test
    fun `창을 닫기 전에 저장 안 된 내용을 저장한 뒤 닫는다`() {
        openAndWait(first.dirName)
        viewModel.updateEditor(TextFieldValue("닫기 전에 쓴 원문"))
        var closed = false

        viewModel.saveThen { closed = true }
        eventually { closed }

        assertEquals("닫기 전에 쓴 원문", Files.readString(first.path.resolve("index.md")))
        assertFalse(viewModel.isDirty)
    }

    private fun openAndWait(dirName: String) {
        viewModel.open(viewModel.posts.first { it.dirName == dirName })
        eventually { viewModel.selected?.dirName == dirName }
    }

    /** [condition]이 참이 될 때까지 최대 5초 기다린다. */
    private fun eventually(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5_000
        while (!condition()) {
            assertTrue(System.currentTimeMillis() < deadline, "5초 안에 조건이 충족되지 않았다")
            Thread.sleep(10)
        }
    }
}
