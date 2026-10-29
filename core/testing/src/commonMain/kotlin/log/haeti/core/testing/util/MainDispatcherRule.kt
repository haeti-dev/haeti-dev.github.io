package log.haeti.core.testing.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * `viewModelScope`가 쓰는 `Dispatchers.Main`을 테스트 디스패처로 바꿔 준다.
 *
 * `commonTest`에는 JUnit 규칙이 없으므로, 테스트가 `@BeforeTest`/`@AfterTest`에서
 * [setUp]·[tearDown]을 직접 호출한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) {
    /** `Dispatchers.Main`을 [testDispatcher]로 교체한다. */
    fun setUp() = Dispatchers.setMain(testDispatcher)

    /** `Dispatchers.Main`을 원래대로 되돌린다. */
    fun tearDown() = Dispatchers.resetMain()
}
