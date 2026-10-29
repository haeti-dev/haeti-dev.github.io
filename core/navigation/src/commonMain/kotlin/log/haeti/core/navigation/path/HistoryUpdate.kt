package log.haeti.core.navigation.path

import androidx.navigation3.runtime.NavKey

/** 지금 화면을 브라우저 주소창에 반영하는 방법. */
enum class HistoryUpdate {
    /** 주소가 이미 지금 화면과 같다. */
    None,

    /** 같은 화면을 가리키지만 모양이 다르다(끝 슬래시 등). 항목을 늘리지 않고 주소만 고친다. */
    Replace,

    /** 다른 화면으로 옮겼다. 새 항목을 쌓는다. */
    Push,
}

/**
 * 주소창이 [appPath]일 때 화면 [current]를 반영하는 방법을 고른다.
 * 경로 문자열이 아니라 가리키는 화면으로 비교해, 외부에서 `/post/foo`로 들어와도 뒤로가기 항목이 늘지 않게 한다.
 *
 * @param appPath 주소창 경로에서 배포 접두사를 뗀 앱 경로
 * @param current 지금 백스택 맨 위 화면
 */
fun historyUpdateFor(
    appPath: String,
    current: NavKey,
): HistoryUpdate =
    when {
        appPath == current.toPath() -> HistoryUpdate.None
        navKeyFromPath(appPath) == current -> HistoryUpdate.Replace
        else -> HistoryUpdate.Push
    }
