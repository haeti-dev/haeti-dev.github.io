package log.haeti.admin.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import log.haeti.admin.io.PostFileStore
import log.haeti.admin.model.PostEntry
import log.haeti.admin.model.TranslationStatus

/**
 * 번역 결과 안내 한 줄.
 *
 * @param message 사용자에게 보일 문장
 * @param failed 실패 안내인지 (색이 달라진다)
 */
data class TranslationNotice(
    val message: String,
    val failed: Boolean,
)

/**
 * 어드민에서 시작한 번역 작업들의 상태. 글마다 동시에 하나만 돌고, 끝나면 결과를 [notice]로 남긴다.
 *
 * @param runner CLI 실행기
 * @param fileStore 끝난 뒤 번역본 상태를 다시 읽을 [PostFileStore]
 */
@Inject
@SingleIn(AppScope::class)
class TranslationQueue(
    private val runner: TranslationRunner,
    private val fileStore: PostFileStore,
) {
    /** 번역이 돌고 있는 글 디렉터리명. */
    var running by mutableStateOf<Set<String>>(emptySet())
        private set

    /** 마지막 번역 결과 안내. 닫으면 null. */
    var notice by mutableStateOf<TranslationNotice?>(null)
        private set

    /** [entry]의 번역이 돌고 있는지. */
    fun isRunning(entry: PostEntry): Boolean = entry.dirName in running

    /** 안내를 닫는다. */
    fun dismissNotice() {
        notice = null
    }

    /** [entry]를 진행 중으로 표시한다. 이미 돌고 있으면 false라서 두 번 눌러도 한 번만 돈다. */
    fun begin(entry: PostEntry): Boolean {
        if (isRunning(entry)) return false
        running = running + entry.dirName
        notice = null
        return true
    }

    /** [begin]한 번역을 돌리고 끝날 때까지 기다린다. 결과는 디스크의 번역본 상태로 판정한다. */
    suspend fun translate(entry: PostEntry) {
        val (error, status) =
            try {
                withContext(Dispatchers.IO) { runner.runSafely(entry) to statusOf(entry) }
            } finally {
                running = running - entry.dirName
            }
        val title = entry.title.ifBlank { entry.dirName }
        notice =
            when {
                error != null -> TranslationNotice("‘$title’ $error", failed = true)
                status != TranslationStatus.UpToDate ->
                    TranslationNotice(
                        "‘$title’ 번역은 끝났지만 아직 최신이 아닙니다. 로그: ${runner.logFileFor(entry).path}",
                        failed = true,
                    )
                else -> TranslationNotice("‘$title’ 영어 번역본을 갱신했습니다. EN 탭에서 확인하세요.", failed = false)
            }
    }

    private fun TranslationRunner.runSafely(entry: PostEntry): String? =
        runCatching {
            run(entry)
        }.getOrElse { it.toString() }

    private fun statusOf(entry: PostEntry): TranslationStatus? =
        runCatching { fileStore.listPosts().firstOrNull { it.dirName == entry.dirName }?.translation }.getOrNull()
}
