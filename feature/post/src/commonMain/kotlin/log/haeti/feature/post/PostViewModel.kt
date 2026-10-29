package log.haeti.feature.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import log.haeti.core.common.result.Result
import log.haeti.core.common.result.flatMapLatestAsResult
import log.haeti.core.data.preferences.UserPreferencesRepository
import log.haeti.core.data.repository.PostRepository
import log.haeti.core.model.Language
import log.haeti.core.model.Post

/** 구독자가 사라진 뒤 업스트림을 유지하는 시간. */
private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * 글 상세 화면의 글 한 편과 그 글에 딸린 에셋을 공급한다.
 * 슬러그는 내비게이션 시점에 정해지므로 Metro의 assisted 주입으로 받는다.
 *
 * @param postRepository 글 Repository
 * @param preferences 화면 언어 설정 Repository
 * @param slug 표시할 글의 URL 슬러그
 */
@AssistedInject
class PostViewModel(
    private val postRepository: PostRepository,
    preferences: UserPreferencesRepository,
    @Assisted val slug: String,
) : ViewModel() {
    /** 글 상세 상태. 구독 직후에는 항상 [PostUiState.Loading]이다. */
    val uiState: StateFlow<PostUiState> =
        // 언어를 바꿔도 Loading으로 돌아가지 않아 읽던 위치가 남는다.
        preferences.language
            .flatMapLatestAsResult { language -> postRepository.observePosts(language).map { language to it } }
            .map { result ->
                when (result) {
                    is Result.Success -> result.data.let { (language, posts) -> postUiStateOf(posts, slug, language) }
                    is Result.Loading -> PostUiState.Loading
                    is Result.Error -> PostUiState.Error
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = PostUiState.Loading,
            )

    /**
     * 본문 이미지 같은 글에 포함된 파일을 읽는다. 글 디렉터리가 기준이라 글을 찾기 전에는 읽을 수 없다.
     *
     * @param relativePath 글 디렉터리 기준 상대경로 (예: `images/foo.webp`)
     * @return 읽은 바이트. 아직 글이 없거나 파일이 없으면 null
     */
    suspend fun readAsset(relativePath: String): ByteArray? {
        val post = (uiState.value as? PostUiState.Success)?.post ?: return null
        return postRepository.readPostAsset(post.meta, relativePath)
    }

    /** Metro의 수동 assisted 팩토리. 슬러그만 화면이 넘기고 나머지는 그래프가 채운다. */
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        /** [slug]에 해당하는 [PostViewModel]을 만든다. */
        fun create(slug: String): PostViewModel
    }
}

/**
 * 최신순 글 목록에서 [slug]의 글과 앞뒤 글을 찾는다. 없으면 [PostUiState.NotFound].
 *
 * @param posts 발행 글(최신순)
 * @param slug 찾을 글의 슬러그
 * @param language 요청한 언어. 번역본이 없으면 [PostUiState.Success.translationMissing]이 참이 된다
 */
internal fun postUiStateOf(
    posts: List<Post>,
    slug: String,
    language: Language = Language.KO,
): PostUiState {
    val index = posts.indexOfFirst { it.meta.slug == slug }
    if (index < 0) return PostUiState.NotFound
    return PostUiState.Success(
        post = posts[index],
        newer = posts.getOrNull(index - 1)?.meta,
        older = posts.getOrNull(index + 1)?.meta,
        translationMissing = posts[index].language != language,
    )
}
