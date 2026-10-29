package log.haeti.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * 내비게이션 이벤트를 받아 [NavigationState]의 백스택을 갱신한다.
 *
 * @param state 갱신 대상 내비게이션 상태
 */
class Navigator(
    val state: NavigationState,
) {
    /**
     * [key] 화면으로 이동한다.
     *
     * 이미 보고 있는 화면이면 무시하고, 스택 어딘가에 있던 화면이면 거기서 빼고 맨 위에 다시 올린다.
     * 같은 키가 스택에 둘 이상 쌓이지 않게 하려는 규칙이다. 홈은 항상 루트여야 하므로
     * 홈으로 가는 이동은 스택을 홈 하나로 되돌린다.
     *
     * @param key 이동할 화면 키
     */
    fun navigate(key: NavKey) {
        if (state.currentKey == key) return
        if (key == HomeNavKey) {
            replaceAll(listOf(HomeNavKey))
            return
        }
        state.backStack.apply {
            remove(key)
            add(key)
        }
    }

    /**
     * 한 단계 뒤로 간다.
     *
     * @return 실제로 뒤로 갔으면 true, 루트뿐이라 뒤로 갈 곳이 없으면 false
     */
    fun goBack(): Boolean {
        val backStack = state.backStack
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.size - 1)
        return true
    }

    /**
     * 백스택을 통째로 [keys]로 바꾼다. 브라우저 뒤로·앞으로(popstate)를 스택에 반영할 때 쓴다.
     *
     * 내용이 이미 같으면 아무것도 하지 않아 화면 상태가 불필요하게 버려지지 않는다.
     *
     * @param keys 새 백스택. 비어 있으면 홈만 남긴 것으로 간주한다.
     */
    fun replaceAll(keys: List<NavKey>) {
        val next = keys.ifEmpty { listOf(HomeNavKey) }
        if (state.backStack.toList() == next) return
        state.backStack.apply {
            clear()
            addAll(next)
        }
    }
}
