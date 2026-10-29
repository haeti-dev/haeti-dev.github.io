package log.haeti.admin.translate

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import log.haeti.admin.io.ContentRoot
import log.haeti.admin.model.PostEntry
import log.haeti.admin.model.TranslationStatus
import java.io.File
import java.util.concurrent.TimeUnit

/** 번역 한 편에 허용하는 최대 시간. 보통 1~3분이다. */
private const val TIMEOUT_MINUTES = 20L

private const val STATUS_SCRIPT = ".claude/skills/haeti-translate-post/scripts/translation_status.py"

/** 번역 세션이 쓸 수 있는 도구. 파일 편집과 스킬의 상태 스크립트 실행만 허용한다. */
private val ALLOWED_TOOLS =
    listOf("Skill", "Read", "Edit", "Write", "Glob", "Grep", "Bash(python3 $STATUS_SCRIPT:*)")

/** GUI로 띄운 앱은 셸 PATH를 물려받지 못하므로 CLI·python3·git·node가 있을 만한 곳을 직접 붙인다. */
private val EXTRA_PATHS: List<String>
    get() {
        val home = System.getProperty("user.home")
        val nvmNode =
            File(home, ".nvm/versions/node")
                .listFiles()
                .orEmpty()
                .maxByOrNull { it.name }
                ?.resolve("bin")
        return listOfNotNull(
            "$home/.local/bin",
            nvmNode?.path,
            "/opt/homebrew/bin",
            "/usr/local/bin",
            "/usr/bin",
            "/bin",
        )
    }

/** `claude -p`가 입력을 기다리지 않도록 stdin을 비운다. */
private val DEV_NULL = File("/dev/null")

/** CLI가 로그인 문제로 멈췄을 때 출력에 나오는 문구. */
private val LOGIN_ERRORS = listOf("Not logged in", "Failed to authenticate", "/login")

/**
 * Claude Code CLI(`claude -p`)를 띄워 `haeti-translate-post` 스킬로 번역본을 만들거나 갱신한다.
 *
 * 어드민이 직접 네트워크를 쓰지 않는 유일한 예외다. 사용자가 배지를 눌렀을 때만 로컬 CLI를 실행한다.
 * 출력은 임시 디렉터리의 로그 파일에 남는다. 블로킹이므로 호출부가 IO 디스패처로 감싼다.
 *
 * @param contentRoot 저장소 루트를 찾을 기준 (`content/`의 부모가 루트다)
 */
@Inject
@SingleIn(AppScope::class)
class TranslationRunner(
    private val contentRoot: ContentRoot,
) {
    /**
     * 번역을 실행하고 끝날 때까지 기다린다.
     *
     * @param entry 번역할 글
     * @return 성공이면 null, 실패면 사용자에게 보일 이유
     */
    fun run(entry: PostEntry): String? {
        val claude = locateClaude() ?: return "claude CLI를 찾지 못했습니다 (~/.local/bin 등)"
        return run(entry, claude)
    }

    /** [claude] 실행 파일로 번역을 돌린다. 테스트는 fake 실행 파일을 넘긴다. */
    internal fun run(
        entry: PostEntry,
        claude: File,
    ): String? {
        val log = logFileFor(entry)
        val command = listOf(claude.path, "-p", promptFor(entry), "--permission-mode", "acceptEdits") + toolArgs()
        val builder = ProcessBuilder(command).directory(contentRoot.path.parent.toFile())
        builder.redirectErrorStream(true).redirectOutput(log).redirectInput(ProcessBuilder.Redirect.from(DEV_NULL))
        builder.environment().cleanForCli()
        val process = builder.start()
        if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            process.descendants().forEach { it.destroyForcibly() }
            process.destroyForcibly()
            return "번역이 ${TIMEOUT_MINUTES}분 안에 끝나지 않았습니다. 로그: ${log.path}"
        }
        if (process.exitValue() == 0) return null
        val output = log.readText()
        return if (LOGIN_ERRORS.any { it in output }) {
            "claude CLI 로그인이 필요합니다. 터미널에서 claude를 실행해 /login 하세요."
        } else {
            "번역이 실패했습니다. 로그: ${log.path}"
        }
    }

    /** 로그 파일 경로. 실패 안내에 그대로 보여 준다. */
    fun logFileFor(entry: PostEntry): File =
        File(
            System.getProperty("java.io.tmpdir"),
            "haeti-translate",
        ).also { it.mkdirs() }.resolve("${entry.dirName}.log")

    private fun locateClaude(): File? =
        pathWithExtras(System.getenv("PATH"))
            .split(File.pathSeparator)
            .map { File(it, "claude") }
            .firstOrNull { it.canExecute() }

    private fun toolArgs(): List<String> = listOf("--allowedTools") + ALLOWED_TOOLS
}

/**
 * CLI에 넘길 환경을 다듬는다. PATH를 추가하고, 어드민을 띄운 Claude 세션의 변수(`CLAUDE*`, 그 세션 전용
 * `ANTHROPIC_BASE_URL`)는 지운다. 남아 있으면 CLI가 자기 로그인 대신 부모 세션 인증을 쓰려다 실패한다.
 */
private fun MutableMap<String, String>.cleanForCli() {
    keys.removeAll { it.startsWith("CLAUDE") || it == "ANTHROPIC_BASE_URL" }
    this["PATH"] = pathWithExtras(this["PATH"])
}

private fun pathWithExtras(current: String?): String =
    (current.orEmpty().split(File.pathSeparator) + EXTRA_PATHS)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(File.pathSeparator)

/** 스킬에 넘길 요청. 사람이 없는 실행이라 되묻지 말고 끝까지 하라고 못 박는다. */
internal fun promptFor(entry: PostEntry): String {
    val dir = "content/posts/${entry.dirName}"
    val task =
        if (entry.translation == TranslationStatus.Missing) {
            "$dir 글의 영어 번역본(index.en.md)을 haeti-translate-post 스킬로 새로 만들어줘."
        } else {
            "$dir 글의 영어 번역본(index.en.md)을 haeti-translate-post 스킬로 갱신해줘. 바뀐 부분만 다시 옮겨."
        }
    return "$task 스킬 절차대로 마지막에 stamp로 원문 지문을 기록해. " +
        "어드민에서 자동 실행한 것이라 질문할 사람이 없으니 되묻지 말고 끝까지 진행해."
}
