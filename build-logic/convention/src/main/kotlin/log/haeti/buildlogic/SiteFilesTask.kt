package log.haeti.buildlogic

import log.haeti.buildlogic.site.ShellPage
import log.haeti.buildlogic.site.SiteInfo
import log.haeti.buildlogic.site.SitePost
import log.haeti.buildlogic.site.publishedPosts
import log.haeti.buildlogic.site.renderFeed
import log.haeti.buildlogic.site.renderRobots
import log.haeti.buildlogic.site.renderShell
import log.haeti.buildlogic.site.renderSitemap
import log.haeti.buildlogic.site.writeCoverOg
import log.haeti.buildlogic.site.writeSiteOg
import log.haeti.buildlogic.site.writeThumbnailOg
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

private const val SITE_IMAGE = "/og/site.png"

/**
 * 정적 호스팅(GitHub Pages)에 함께 올릴 파일을 만든다. 캔버스 앱은 HTML에 본문이 없으므로
 * 링크 미리보기·검색·구독은 모두 이 파일들이 맡는다.
 *
 * - `index.html`·`404.html`: 사이트 OG가 들어간 셸
 * - `post/<slug>/index.html`: 글마다 제목·요약·썸네일 OG가 들어간 셸. 딥링크 새로고침도 404 없이 열린다
 * - `<고정 페이지>/index.html`: 소개·프로젝트처럼 [staticPages]의 셸. 없으면 Pages가 404 상태로 내려 검색에서 빠진다
 * - `og/`: 미리보기 이미지. 썸네일이 있으면 그 JPG, 없으면 블로그와 같은 코드 미니맵 PNG
 * - `sitemap.xml`·`feed.xml`(RSS)·`robots.txt`
 */
@CacheableTask
abstract class SiteFilesTask : DefaultTask() {
    /** `content/posts` 디렉터리. 초안은 제외한다. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val contentDirectory: DirectoryProperty

    /** 배포 번들의 index.html. `<base href>`가 이미 채워져 있고 [log.haeti.buildlogic.site.HEAD_MARKER] 자리가 있다. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val template: RegularFileProperty

    /** 사이트 카드 워드마크 글꼴. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val boldFont: RegularFileProperty

    /** 사이트 카드 소개 글꼴. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val regularFont: RegularFileProperty

    /** 끝 `/` 없는 사이트 절대 주소. OG·사이트맵·RSS는 절대 URL이어야 한다. */
    @get:Input
    abstract val siteUrl: Property<String>

    /** 사이트 이름. */
    @get:Input
    abstract val siteName: Property<String>

    /** 사이트 한 줄 소개. 마침표로 끊어 사이트 카드에 줄을 나눈다. */
    @get:Input
    abstract val siteDescription: Property<String>

    /** 고정 페이지 경로(`/about/`)와 문서 제목. 셸을 만들고 사이트맵에 넣는다. */
    @get:Input
    abstract val staticPages: MapProperty<String, String>

    /** 생성 파일 루트. 배포 디렉터리에 그대로 겹쳐 복사한다. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun generate() {
        // 이미지를 그리는 AWT가 macOS에서 Dock 아이콘을 띄우지 않게 한다. 데몬은 화면이 필요 없다.
        System.setProperty("java.awt.headless", "true")
        val output = outputDirectory.get().asFile
        fileSystemOperations.delete { delete(output) }
        output.resolve("og").mkdirs()
        val site = SiteInfo(siteUrl.get().trimEnd('/'), siteName.get(), siteDescription.get())
        val posts = publishedPosts(contentDirectory.get().asFile)
        val template = template.get().asFile.readText()

        writeSiteOg(site, boldFont.get().asFile, regularFont.get().asFile, output.resolve("og/site.png"))
        val home = renderShell(template, ShellPage("/", site.name, site.description, SITE_IMAGE), site)
        output.resolve("index.html").writeText(home)
        output.resolve("404.html").writeText(home)
        staticPages.get().forEach { (path, title) ->
            val page = ShellPage(path, title, site.description, SITE_IMAGE, isStatic = true)
            val shell = output.resolve(path.trim('/')).apply { mkdirs() }.resolve("index.html")
            shell.writeText(renderShell(template, page, site))
        }
        posts.forEach { post ->
            val page = ShellPage(post.path, post.title, post.summary, writePostImage(post, output), post)
            output.resolve("post/${post.slug}").apply { mkdirs() }.resolve("index.html")
                .writeText(renderShell(template, page, site))
        }
        output.resolve("sitemap.xml").writeText(renderSitemap(site, staticPages.get().keys.toList(), posts))
        output.resolve("feed.xml").writeText(renderFeed(site, posts))
        output.resolve("robots.txt").writeText(renderRobots(site))
        logger.lifecycle("배포 파일: 글 ${posts.size}편의 OG 셸, sitemap.xml, feed.xml (${site.url})")
    }

    /** 글의 미리보기 이미지를 만들고 사이트 루트 기준 경로를 돌려준다. */
    private fun writePostImage(
        post: SitePost,
        output: File,
    ): String {
        val thumbnail = post.thumbnail?.let(post.directory::resolve)?.takeIf { it.isFile }
        if (thumbnail != null) {
            val path = "/og/${post.slug}.jpg"
            val written = runCatching { writeThumbnailOg(thumbnail, output.resolve(path.drop(1))) }
            if (written.isSuccess) return path
            logger.warn("${post.slug}: 썸네일을 읽지 못해 코드 미니맵으로 대신합니다 (${written.exceptionOrNull()?.message})")
        }
        val path = "/og/${post.slug}.png"
        writeCoverOg(post, output.resolve(path.drop(1)))
        return path
    }
}
