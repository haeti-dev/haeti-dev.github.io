package log.haeti.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * 새로고침·프로세스 종료 뒤에도 복원되는 내비게이션 상태를 만든다.
 *
 * @param startKey 백스택 루트가 될 시작 화면 키
 */
@Composable
fun rememberNavigationState(startKey: NavKey = HomeNavKey): NavigationState = rememberNavigationState(listOf(startKey))

/**
 * 초기 백스택을 통째로 지정해 내비게이션 상태를 만든다.
 * 브라우저 딥링크(`/post/<slug>`)로 들어왔을 때 홈을 먼저 그렸다가 바꾸는 깜빡임 없이
 * `[Home, Post]`로 바로 시작하기 위해 쓴다.
 *
 * @param initialStack 처음 백스택. 비어 있으면 홈만 둔다
 */
@Composable
fun rememberNavigationState(initialStack: List<NavKey>): NavigationState {
    val keys = initialStack.ifEmpty { listOf(HomeNavKey) }
    val backStack = rememberNavBackStack(HaetiSavedStateConfiguration, *keys.toTypedArray())
    return remember(backStack) { NavigationState(backStack) }
}

/**
 * 단일 백스택을 감싼 내비게이션 상태 홀더.
 *
 * 블로그는 탭마다 별도 히스토리를 유지할 필요가 없어 스택을 하나만 둔다.
 *
 * @param backStack 화면 키 스택. 항상 원소를 하나 이상 유지한다.
 */
class NavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    /** 지금 보이는 화면 키. 백스택의 마지막 원소. */
    val currentKey: NavKey by derivedStateOf { backStack.last() }
}
