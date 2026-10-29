package log.haeti.web.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.datetime.LocalDate
import log.haeti.core.designsystem.HaetiTheme
import log.haeti.core.designsystem.rememberHaetiFontsReady
import log.haeti.core.model.Language
import log.haeti.core.navigation.NavigationState
import log.haeti.core.navigation.Navigator
import log.haeti.core.navigation.path.documentTitle
import log.haeti.core.navigation.toTopLevelDestination
import log.haeti.core.ui.component.HaetiTopBar
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.chromeMaxWidth
import log.haeti.core.ui.layout.layoutTierOf
import log.haeti.core.ui.platform.LocalLastUpdated
import log.haeti.core.ui.platform.LocalPostAssetLoader
import log.haeti.core.ui.platform.PostAssetLoader
import log.haeti.core.ui.strings.LocalHaetiStrings
import log.haeti.core.ui.strings.haetiStrings
import log.haeti.feature.home.navigation.homeEntry
import log.haeti.feature.pages.navigation.pagesEntries
import log.haeti.feature.post.navigation.postEntry
import log.haeti.web.di.AppGraph

/**
 * 플랫폼에 의존하지 않는 앱 셸. 테마·상단바·내비게이션 표시를 조립한다.
 *
 * 뷰포트 폭 측정은 여기 한 곳에서만 하고 결과를 `LocalLayoutTier`로 내려보낸다.
 * 브라우저 히스토리·문서 제목처럼 플랫폼이 필요한 일은 [onTitleChanged]처럼 콜백으로 위임한다.
 *
 * @param appGraph ViewModel 팩토리를 꺼낼 객체 그래프
 * @param navigationState 현재 백스택
 * @param navigator 화면 이동 명령 수신자
 * @param themeController 다크 테마 상태
 * @param onTitleChanged 문서 제목이 바뀔 때마다 호출. 브라우저는 `document.title`에 반영한다
 * @param onLanguageChanged 화면 언어가 바뀔 때마다(첫 언어 포함) 호출. 브라우저는 `<html lang>`에 반영한다
 * @param onLanguageSelected 사용자가 언어 토글을 눌렀을 때만 호출. 브라우저는 선택을 저장한다
 */
@Composable
fun HaetiAppShell(
    appGraph: AppGraph,
    navigationState: NavigationState,
    navigator: Navigator,
    themeController: ThemeController,
    onTitleChanged: (String) -> Unit,
    onLanguageChanged: (Language) -> Unit = {},
    onLanguageSelected: (Language) -> Unit = {},
) {
    val repository = appGraph.postRepository
    val preferences = appGraph.userPreferences
    val language by preferences.language.collectAsState()
    LaunchedEffect(language) { onLanguageChanged(language) }
    val lastUpdated by produceState<LocalDate?>(initialValue = null, repository) {
        repository.observePosts().collect { posts -> value = posts.firstOrNull()?.meta?.date }
    }
    val assetLoader = remember(repository) { PostAssetLoader(repository::readPostAsset) }
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
        LocalPostAssetLoader provides assetLoader,
        LocalLastUpdated provides lastUpdated,
        LocalHaetiStrings provides haetiStrings(language),
    ) {
        HaetiTheme(darkTheme = themeController.isDark) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                // 웹 폰트가 도착하기 전에 텍스트를 그리면 한글이 tofu로 남는다. 준비될 때까지 빈 표면 유지.
                if (!rememberHaetiFontsReady()) return@Surface
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalLayoutTier provides layoutTierOf(maxWidth)) {
                        ShellContent(
                            navigationState = navigationState,
                            navigator = navigator,
                            themeController = themeController,
                            onSelectLanguage = { selected ->
                                preferences.setLanguage(selected)
                                onLanguageSelected(selected)
                            },
                            onTitleChanged = onTitleChanged,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 상단바와 화면 영역을 세로로 쌓는다. 글 제목은 상세 화면이 콜백으로 알려줄 때까지 비어 있다.
 *
 * @param navigationState 현재 백스택
 * @param navigator 화면 이동 명령 수신자
 * @param themeController 다크 테마 상태
 * @param onSelectLanguage 상단바 언어 토글을 눌렀을 때 호출
 * @param onTitleChanged 문서 제목 변경 콜백
 */
@Composable
private fun ShellContent(
    navigationState: NavigationState,
    navigator: Navigator,
    themeController: ThemeController,
    onSelectLanguage: (Language) -> Unit,
    onTitleChanged: (String) -> Unit,
) {
    val currentKey = navigationState.currentKey
    val strings = LocalHaetiStrings.current
    var postTitle by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentKey) { postTitle = null }
    LaunchedEffect(currentKey, postTitle, strings) {
        onTitleChanged(currentKey.documentTitle(postTitle, strings.pageTitles))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HaetiTopBar(
            activeDestination = currentKey.toTopLevelDestination(),
            onNavigate = { destination -> navigator.navigate(destination.navKey) },
            isDarkTheme = themeController.isDark,
            onToggleTheme = themeController::toggle,
            maxWidth = LocalLayoutTier.current.chromeMaxWidth,
            language = strings.language,
            onSelectLanguage = onSelectLanguage,
        )
        NavDisplay(
            backStack = navigationState.backStack,
            modifier = Modifier.weight(1f),
            onBack = { navigator.goBack() },
            transitionSpec = { screenTransition() },
            popTransitionSpec = { screenTransition() },
            predictivePopTransitionSpec = { screenTransition() },
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    rememberViewModelStoreNavEntryDecorator<NavKey>(),
                ),
            entryProvider =
                entryProvider {
                    homeEntry(navigator)
                    postEntry(navigator) { title -> postTitle = title }
                    pagesEntries(navigator)
                },
        )
    }
}
