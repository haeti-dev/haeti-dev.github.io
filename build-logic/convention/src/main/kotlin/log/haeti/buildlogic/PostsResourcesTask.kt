package log.haeti.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

private const val INDEX_FILE = "index.md"

/**
 * `content/posts/`(원본, source of truth)를 Compose 리소스 디렉터리로 변환한다.
 *
 * 출력은 `files/posts/<yyyy-MM-dd-slug>/…`와 글 디렉터리명을 최신순으로 담은
 * `files/posts/index.json`이다. 프론트매터에 `draft: true`가 있는 글은 복사도 색인도 하지 않아
 * 배포 번들에 실리지 않는다. 글마다 [checkPost]로 검사해 규칙을 어긴 글이 있으면 빌드를 멈춘다. 소스 트리에는 아무것도 복사하지 않고 `customDirectory`로 연결한다.
 * [sampleDirectory]를 지정하면(개발 서버 전용) 레이아웃 확인용 샘플 글도 함께 싣는다.
 */
@CacheableTask
abstract class PostsResourcesTask : DefaultTask() {
    /** `content/posts` 디렉터리. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val contentDirectory: DirectoryProperty

    /** 레이아웃 확인용 샘플 글 디렉터리(`content/samples`). 배포 빌드에서는 비워 둔다. */
    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sampleDirectory: DirectoryProperty

    /** 생성된 리소스 루트. Compose Resources의 `customDirectory`로 등록된다. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun generate() {
        val output = outputDirectory.get().asFile
        fileSystemOperations.delete { delete(output) }
        val postsOutput = output.resolve("files/posts").also { it.mkdirs() }

        val sources = listOfNotNull(contentDirectory.get().asFile, sampleDirectory.orNull?.asFile)
        val published = sources.flatMap { it.listFiles().orEmpty().toList() }
            .filter { it.isDirectory && it.resolve(INDEX_FILE).isFile }
            .filterNot { checkPost(it).draft }
            .sortedByDescending { it.name }

        published.forEach { postDirectory ->
            fileSystemOperations.copy {
                from(postDirectory)
                into(postsOutput.resolve(postDirectory.name))
                exclude(".DS_Store")
            }
        }
        postsOutput.resolve("index.json").writeText(
            published.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"${it.name}\"" },
        )
    }
}
