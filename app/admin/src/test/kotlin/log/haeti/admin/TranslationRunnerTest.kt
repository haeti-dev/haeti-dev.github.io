package log.haeti.admin

import log.haeti.admin.io.ContentRoot
import log.haeti.admin.model.PostEntry
import log.haeti.admin.model.TranslationStatus
import log.haeti.admin.translate.TranslationRunner
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TranslationRunnerTest {
    private val repo = Files.createTempDirectory("haeti-repo").toFile().also { it.resolve("content").mkdirs() }
    private val runner = TranslationRunner(ContentRoot(repo.resolve("content").toPath()))
    private val entry =
        PostEntry(
            dirName = "2026-10-09-test",
            path = repo.resolve("content/posts/2026-10-09-test").toPath(),
            translation = TranslationStatus.Stale,
        )

    @Test
    fun 저장소_루트에서_스킬_요청과_허용_도구로_CLI를_띄운다() {
        val record = repo.resolve("record.txt")
        val claude = fakeClaude("pwd > '$record'; printf '%s\\n' \"\$@\" >> '$record'; exit 0")

        assertNull(runner.run(entry, claude))

        val lines = record.readLines()
        assertEquals(repo.canonicalPath, File(lines[0]).canonicalPath)
        assertEquals("-p", lines[1])
        assertTrue("content/posts/2026-10-09-test" in lines[2] && "갱신" in lines[2])
        assertTrue(lines.containsAll(listOf("--permission-mode", "acceptEdits", "--allowedTools", "Edit")))
        assertTrue(lines.any { it.startsWith("Bash(python3 .claude/skills/haeti-translate-post/") })
    }

    @Test
    fun 로그인_문제면_로그인_안내를_돌려준다() {
        val claude = fakeClaude("echo 'Not logged in · Please run /login'; exit 1")

        val error = runner.run(entry, claude)

        assertTrue(error != null && "/login" in error)
    }

    @Test
    fun 그_밖의_실패는_로그_경로를_알려준다() {
        val claude = fakeClaude("echo boom; exit 3")

        val error = runner.run(entry, claude)

        assertTrue(error != null && runner.logFileFor(entry).path in error)
    }

    private fun fakeClaude(body: String): File =
        repo.resolve("claude").apply {
            writeText("#!/bin/sh\n$body\n")
            setExecutable(true)
        }
}
