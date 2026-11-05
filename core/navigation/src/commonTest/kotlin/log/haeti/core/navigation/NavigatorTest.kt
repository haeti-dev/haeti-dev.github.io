package log.haeti.core.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** [Navigator]가 단일 백스택을 중복 없이 갱신하는지 확인한다. */
class NavigatorTest {
    private lateinit var navigationState: NavigationState
    private lateinit var navigator: Navigator

    private val firstPost = PostNavKey("hello-haeti-log")
    private val secondPost = PostNavKey("second-post")

    @BeforeTest
    fun setUp() {
        navigationState = NavigationState(NavBackStack<NavKey>(HomeNavKey))
        navigator = Navigator(navigationState)
    }

    @Test
    fun startsAtHome() {
        assertEquals(listOf(HomeNavKey), navigationState.backStack.toList())
        assertEquals(HomeNavKey, navigationState.currentKey)
    }

    @Test
    fun navigatePushesKey() {
        navigator.navigate(firstPost)

        assertEquals(listOf(HomeNavKey, firstPost), navigationState.backStack.toList())
        assertEquals(firstPost, navigationState.currentKey)
    }

    @Test
    fun navigateToCurrentKeyIsIgnored() {
        navigator.navigate(firstPost)

        val writes = backStackWritesDuring { navigator.navigate(firstPost) }

        assertEquals(listOf(HomeNavKey, firstPost), navigationState.backStack.toList())
        // 빼고 다시 넣어도 내용은 같다. 무시했는지는 스택을 건드렸는지로 본다.
        assertEquals(0, writes)
    }

    @Test
    fun navigateMovesExistingKeyToTopWithoutDuplicating() {
        navigator.navigate(firstPost)
        navigator.navigate(secondPost)
        navigator.navigate(firstPost)

        assertEquals(listOf(HomeNavKey, secondPost, firstPost), navigationState.backStack.toList())
    }

    @Test
    fun navigateToHomeResetsStackToHomeRoot() {
        navigator.navigate(ProjectsNavKey)
        navigator.navigate(HomeNavKey)

        assertEquals(listOf(HomeNavKey), navigationState.backStack.toList())
        assertEquals(HomeNavKey, navigationState.currentKey)
    }

    @Test
    fun goBackPopsOneEntry() {
        navigator.navigate(firstPost)

        assertTrue(navigator.goBack())
        assertEquals(listOf(HomeNavKey), navigationState.backStack.toList())
        assertEquals(HomeNavKey, navigationState.currentKey)
    }

    @Test
    fun goBackAtRootReturnsFalse() {
        assertFalse(navigator.goBack())
        assertEquals(listOf(HomeNavKey), navigationState.backStack.toList())
    }

    @Test
    fun replaceAllSwapsWholeStack() {
        navigator.navigate(firstPost)

        navigator.replaceAll(listOf(HomeNavKey, AboutNavKey))

        assertEquals(listOf(HomeNavKey, AboutNavKey), navigationState.backStack.toList())
        assertEquals(AboutNavKey, navigationState.currentKey)
    }

    @Test
    fun replaceAllWithEmptyListKeepsOnlyHome() {
        navigator.navigate(firstPost)

        navigator.replaceAll(emptyList())

        assertEquals(listOf(HomeNavKey), navigationState.backStack.toList())
    }

    @Test
    fun replaceAllWithSameStackKeepsIdentity() {
        navigator.navigate(firstPost)
        val before = navigationState.backStack.toList()

        val writes = backStackWritesDuring { navigator.replaceAll(before) }

        assertEquals(before, navigationState.backStack.toList())
        // 비웠다가 같은 내용으로 다시 채우면 내용은 같아도 화면 상태가 버려진다.
        assertEquals(0, writes)
    }

    @Test
    fun backStackWritesAreCounted() {
        // 아래 세는 도구가 실제 쓰기를 놓치지 않는지 확인한다. 못 세면 위의 0은 아무것도 증명하지 못한다.
        assertTrue(backStackWritesDuring { navigator.navigate(secondPost) } > 0)
    }

    /** [block] 동안 백스택에 일어난 스냅숏 쓰기 수. */
    private fun backStackWritesDuring(block: () -> Unit): Int {
        Snapshot.sendApplyNotifications()
        var writes = 0
        val observer = Snapshot.registerApplyObserver { changed, _ -> writes += changed.size }
        try {
            block()
            Snapshot.sendApplyNotifications()
        } finally {
            observer.dispose()
        }
        return writes
    }
}
