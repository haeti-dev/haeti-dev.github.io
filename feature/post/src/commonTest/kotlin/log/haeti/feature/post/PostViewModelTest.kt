package log.haeti.feature.post

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import log.haeti.core.model.Language
import log.haeti.core.testing.data.TestPosts
import log.haeti.core.testing.repository.FakePostRepository
import log.haeti.core.testing.repository.TestUserPreferencesRepository
import log.haeti.core.testing.util.MainDispatcherRule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `stateIn` 흐름이라 값이 갱신되려면 구독자가 있어야 한다.
 * 그래서 각 테스트는 `backgroundScope`에서 `uiState`를 먼저 구독한다.
 */
class PostViewModelTest {
    private val mainDispatcherRule = MainDispatcherRule()
    private val postRepository = FakePostRepository()
    private lateinit var viewModel: PostViewModel

    @BeforeTest
    fun setUp() {
        mainDispatcherRule.setUp()
        viewModel =
            PostViewModel(
                postRepository = postRepository,
                preferences = TestUserPreferencesRepository(),
                slug = TestPosts.hello.meta.slug,
            )
    }

    @AfterTest
    fun tearDown() = mainDispatcherRule.tearDown()

    @Test
    fun `슬러그는 주입받은 값 그대로다`() = assertEquals(TestPosts.hello.meta.slug, viewModel.slug)

    @Test
    fun `구독 전에는 로딩이다`() =
        runTest {
            assertEquals(PostUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun `슬러그가 맞는 글을 찾는다`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            postRepository.sendPosts(TestPosts.all)

            val state = viewModel.uiState.value
            assertIs<PostUiState.Success>(state)
            assertEquals(TestPosts.hello, state.post)
        }

    @Test
    fun `없는 슬러그는 찾지 못함이다`() =
        runTest {
            val missing =
                PostViewModel(
                    postRepository = postRepository,
                    preferences = TestUserPreferencesRepository(),
                    slug = "없는-슬러그",
                )
            backgroundScope.launch(UnconfinedTestDispatcher()) { missing.uiState.collect() }

            postRepository.sendPosts(TestPosts.all)

            assertEquals(PostUiState.NotFound, missing.uiState.value)
        }

    @Test
    fun `글을 찾은 뒤에는 에셋 바이트를 돌려준다`() =
        runTest {
            val bytes = byteArrayOf(1, 2, 3)
            postRepository.putAsset(TestPosts.hello.meta, "images/sample.webp", bytes)
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            postRepository.sendPosts(TestPosts.all)

            assertContentEquals(bytes, viewModel.readAsset("images/sample.webp"))
        }

    @Test
    fun `글을 찾기 전에는 에셋을 읽지 않는다`() =
        runTest {
            assertNull(viewModel.readAsset("images/sample.webp"))
        }

    @Test
    fun `앞뒤 글을 함께 찾는다`() {
        val state = postUiStateOf(TestPosts.all, TestPosts.second.meta.slug)

        assertIs<PostUiState.Success>(state)
        assertEquals(TestPosts.hello.meta, state.newer)
        assertNull(state.older)
    }

    @Test
    fun `영어를 요청했는데 원문만 있으면 번역 없음 표시를 켠다`() {
        val state = postUiStateOf(TestPosts.all, TestPosts.hello.meta.slug, Language.EN)

        assertIs<PostUiState.Success>(state)
        assertTrue(state.translationMissing)
    }
}
