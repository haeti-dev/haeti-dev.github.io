package log.haeti.feature.home

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import log.haeti.core.data.repository.PostRepository
import log.haeti.core.model.Language
import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta
import log.haeti.core.testing.data.TestPosts
import log.haeti.core.testing.repository.FakePostRepository
import log.haeti.core.testing.repository.TestUserPreferencesRepository
import log.haeti.core.testing.util.MainDispatcherRule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/** 목록 흐름이 처음부터 터지는 상황을 만드는 Fake Repository. */
private class FailingPostRepository : PostRepository {
    override fun observePosts(language: Language): Flow<List<Post>> = flow { throw IllegalStateException("목록을 읽지 못했다") }

    override fun observePost(
        slug: String,
        language: Language,
    ): Flow<Post?> = flow { throw IllegalStateException("글을 읽지 못했다") }

    override suspend fun readPostAsset(
        post: PostMeta,
        relativePath: String,
    ): ByteArray? = null
}

/** 한국어는 바로 주고, 다른 언어는 아직 읽는 중인 채로 멈춰 있는 Fake Repository. */
private class SlowTranslationRepository(
    private val posts: List<Post>,
) : PostRepository {
    override fun observePosts(language: Language): Flow<List<Post>> =
        if (language == Language.KO) flowOf(posts) else flow { awaitCancellation() }

    override fun observePost(
        slug: String,
        language: Language,
    ): Flow<Post?> = flowOf(null)

    override suspend fun readPostAsset(
        post: PostMeta,
        relativePath: String,
    ): ByteArray? = null
}

/**
 * `stateIn`으로 만든 흐름이라 값이 갱신되려면 구독자가 있어야 한다.
 * 그래서 각 테스트는 `backgroundScope`에서 `uiState`를 먼저 구독한다.
 */
class HomeViewModelTest {
    private val mainDispatcherRule = MainDispatcherRule()
    private val postRepository = FakePostRepository()
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setUp() {
        mainDispatcherRule.setUp()
        viewModel = HomeViewModel(postRepository = postRepository, preferences = TestUserPreferencesRepository())
    }

    @AfterTest
    fun tearDown() = mainDispatcherRule.tearDown()

    @Test
    fun `구독 전에는 로딩이다`() =
        runTest {
            assertEquals(HomeUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun `전체 보기에서는 최신 글을 강조하고 나머지를 목록에 둔다`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            postRepository.sendPosts(TestPosts.all)

            val state = viewModel.uiState.value
            assertIs<HomeUiState.Success>(state)
            assertEquals(TestPosts.hello.meta, state.featured)
            assertEquals(listOf(TestPosts.second.meta), state.posts)
            assertEquals(2, state.total)
        }

    @Test
    fun `카테고리를 고르면 그 글만 남고 강조는 사라진다`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
            postRepository.sendPosts(TestPosts.all)

            viewModel.selectCategory(TestPosts.second.meta.category)

            val state = viewModel.uiState.value
            assertIs<HomeUiState.Success>(state)
            assertNull(state.featured)
            assertEquals(listOf(TestPosts.second.meta), state.posts)
            assertEquals(TestPosts.second.meta.category, state.selectedCategory)
        }

    @Test
    fun `고른 카테고리를 다시 고르면 전체로 돌아간다`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
            postRepository.sendPosts(TestPosts.all)

            viewModel.selectCategory(TestPosts.second.meta.category)
            viewModel.selectCategory(TestPosts.second.meta.category)

            val state = viewModel.uiState.value
            assertIs<HomeUiState.Success>(state)
            assertNull(state.selectedCategory)
            assertEquals(TestPosts.hello.meta, state.featured)
        }

    @Test
    fun `글이 없어도 성공이다`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            postRepository.sendPosts(emptyList())

            val state = viewModel.uiState.value
            assertIs<HomeUiState.Success>(state)
            assertEquals(emptyList(), state.posts)
            assertNull(state.featured)
        }

    @Test
    fun `저장소가 실패하면 에러다`() =
        runTest {
            val failingViewModel =
                HomeViewModel(postRepository = FailingPostRepository(), preferences = TestUserPreferencesRepository())
            backgroundScope.launch(UnconfinedTestDispatcher()) { failingViewModel.uiState.collect() }

            assertEquals(HomeUiState.Error, failingViewModel.uiState.value)
        }

    @Test
    fun `언어를 바꾸는 동안에는 로딩으로 돌아가지 않고 이전 목록을 유지한다`() =
        runTest {
            val preferences = TestUserPreferencesRepository()
            val slowViewModel = HomeViewModel(SlowTranslationRepository(TestPosts.all), preferences)
            backgroundScope.launch(UnconfinedTestDispatcher()) { slowViewModel.uiState.collect() }

            preferences.setLanguage(Language.EN)

            val state = slowViewModel.uiState.value
            assertIs<HomeUiState.Success>(state)
            assertEquals(TestPosts.hello.meta, state.featured)
        }
}
