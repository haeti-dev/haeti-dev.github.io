package log.haeti.core.common.result

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * 비동기 로딩의 세 가지 상태를 감싸는 래퍼.
 *
 * @param T 성공했을 때 담기는 값의 타입
 */
sealed interface Result<out T> {
    /**
     * 로딩 성공.
     *
     * @param data 불러온 값
     */
    data class Success<T>(
        val data: T,
    ) : Result<T>

    /**
     * 로딩 실패.
     *
     * @param exception 실패 원인
     */
    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>

    /** 로딩 진행 중. 구독 직후 항상 한 번 방출된다. */
    data object Loading : Result<Nothing>
}

/**
 * 업스트림 방출을 [Result]로 감싼다.
 *
 * 구독하는 순간 [Result.Loading]을 먼저 방출하고, 값은 [Result.Success], 예외는 [Result.Error]로 바꾼다.
 * 예외를 흡수하므로 이 흐름을 구독하는 쪽은 별도 try-catch가 필요 없다.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }

/**
 * 키가 바뀔 때마다 [transform]의 흐름으로 갈아타고 [Result]로 감싼다.
 *
 * [Result.Loading]은 구독 직후 한 번만 방출한다. 키(예: 화면 언어)가 바뀌는 동안에는 직전 값이 남아
 * 화면이 스켈레톤으로 돌아가지 않는다. 예외는 그 키의 흐름만 [Result.Error]로 끝내므로 다음 키에서 다시 시도한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <K, T> Flow<K>.flatMapLatestAsResult(transform: suspend (K) -> Flow<T>): Flow<Result<T>> =
    flatMapLatest { key ->
        transform(key).map<T, Result<T>> { Result.Success(it) }.catch { emit(Result.Error(it)) }
    }.onStart { emit(Result.Loading) }
