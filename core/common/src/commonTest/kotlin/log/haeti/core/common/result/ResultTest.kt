package log.haeti.core.common.result

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** [asResult]가 Loading을 먼저 방출하고 값·예외를 각각 Success·Error로 바꾸는지 확인한다. */
class ResultTest {
    @Test
    fun asResultEmitsLoadingThenSuccess() =
        runTest {
            flowOf(1, 2).asResult().test {
                assertEquals(Result.Loading, awaitItem())
                assertEquals(Result.Success(1), awaitItem())
                assertEquals(Result.Success(2), awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun asResultEmitsErrorWhenUpstreamThrows() =
        runTest {
            val failure = IllegalStateException("불러오기 실패")

            flow<Int> { throw failure }.asResult().test {
                assertEquals(Result.Loading, awaitItem())
                val item = awaitItem()
                assertTrue(item is Result.Error)
                assertEquals(failure, item.exception)
                awaitComplete()
            }
        }

    @Test
    fun asResultEmitsValuesBeforeError() =
        runTest {
            flow {
                emit(1)
                throw IllegalStateException("중간에 실패")
            }.asResult().test {
                assertEquals(Result.Loading, awaitItem())
                assertEquals(Result.Success(1), awaitItem())
                assertTrue(awaitItem() is Result.Error)
                awaitComplete()
            }
        }

    @Test
    fun `키가 바뀌어도 Loading은 처음 한 번만 방출한다`() =
        runTest {
            val language = MutableStateFlow("ko")
            language.flatMapLatestAsResult { flowOf("글-$it") }.test {
                assertEquals(Result.Loading, awaitItem())
                assertEquals(Result.Success("글-ko"), awaitItem())
                language.value = "en"
                assertEquals(Result.Success("글-en"), awaitItem())
            }
        }

    @Test
    fun `한 키에서 실패해도 다음 키에서 다시 시도한다`() =
        runTest {
            val language = MutableStateFlow("ko")
            language
                .flatMapLatestAsResult { key -> if (key == "ko") flow { error("실패") } else flowOf(key) }
                .test {
                    assertEquals(Result.Loading, awaitItem())
                    assertTrue(awaitItem() is Result.Error)
                    language.value = "en"
                    assertEquals(Result.Success("en"), awaitItem())
                }
        }
}
