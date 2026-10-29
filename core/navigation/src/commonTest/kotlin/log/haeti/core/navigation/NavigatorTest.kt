package log.haeti.core.navigation

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
        navigator.navigate(firstPost)

        assertEquals(listOf(HomeNavKey, firstPost), navigationState.backStack.toList())
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

        navigator.replaceAll(before)

        assertEquals(before, navigationState.backStack.toList())
    }
}
