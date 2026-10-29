package log.haeti.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import log.haeti.core.common.result.Result
import log.haeti.core.common.result.flatMapLatestAsResult
import log.haeti.core.data.preferences.UserPreferencesRepository
import log.haeti.core.data.repository.PostRepository

/** 구독자가 사라진 뒤 업스트림을 유지하는 시간. */
private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * 홈 화면의 글 목록과 카테고리 필터·검색 상태를 공급한다.
 *
 * @param postRepository 글 Repository
 * @param preferences 화면 언어 설정 Repository
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(HomeViewModel::class)
class HomeViewModel(
    postRepository: PostRepository,
    preferences: UserPreferencesRepository,
) : ViewModel() {
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")

    /** 홈 화면 상태. 구독 직후에는 항상 [HomeUiState.Loading]이다. */
    val uiState: StateFlow<HomeUiState> =
        combine(
            // 언어를 바꿔도 Loading으로 돌아가지 않아 검색창과 스크롤이 제자리에 남는다.
            preferences.language.flatMapLatestAsResult { language ->
                postRepository.observePosts(language).map(::SearchIndex)
            },
            selectedCategory,
            query,
        ) { result, selected, text ->
            when (result) {
                is Result.Success -> homeUiStateOf(result.data, selected, text)
                is Result.Loading -> HomeUiState.Loading
                is Result.Error -> HomeUiState.Error
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState.Loading,
        )

    /**
     * 목록을 카테고리로 거른다. 이미 고른 카테고리를 다시 고르면 전체로 돌아간다.
     *
     * @param category 고를 카테고리. null이면 전체
     */
    fun selectCategory(category: String?) {
        selectedCategory.value = category.takeIf { it != selectedCategory.value }
    }

    /**
     * 제목·요약·카테고리·본문에서 글을 찾는다. 빈 문자열이면 검색을 끝낸다.
     *
     * @param text 검색어
     */
    fun search(text: String) {
        query.value = text
    }
}
