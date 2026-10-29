package log.haeti.core.data.source

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `generatePostsResources` 산출물이 실제로 `Res.readBytes`로 읽히는지 확인한다.
 *
 * 번들 내용이 아니라 배선(빌드 태스크 → `customDirectory` → `Res`)을 검증하므로
 * 글이 늘고 줄어도 깨지지 않게 느슨하게 단언한다.
 */
class ComposeResourcesPostSourceTest {
    private val source = ComposeResourcesPostSource()

    @Test
    fun `인덱스를 읽고 모든 항목이 날짜 접두 디렉터리명이다`() =
        runTest {
            val index = source.readIndex()
            assertTrue(index.isNotEmpty(), "index.json이 비어 있다. generatePostsResources 배선 확인 필요")
            index.forEach { directory ->
                assertTrue(Regex("""^\d{4}-\d{2}-\d{2}-.+""").matches(directory), "디렉터리명 형식 위반: $directory")
            }
        }

    @Test
    fun `인덱스의 첫 글 원문을 프론트매터째로 읽는다`() =
        runTest {
            val raw = assertNotNull(source.readMarkdown(source.readIndex().first()))
            assertEquals("---", raw.lineSequence().first())
        }

    @Test
    fun `없는 글과 없는 에셋은 null이다`() =
        runTest {
            assertNull(source.readMarkdown("9999-99-99-없는글"))
            assertNull(source.readAsset(source.readIndex().first(), "images/없음.webp"))
        }
}
