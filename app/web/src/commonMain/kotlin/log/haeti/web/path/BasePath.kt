package log.haeti.web.path

/** 접두사가 비었거나 해석에 실패했을 때 쓰는 기본값(루트 배포). */
private const val ROOT = "/"

/**
 * 배포 경로 접두사. `index.html`의 `<base href>` 값에서 유도한다.
 *
 * 루트 도메인 배포면 `/`, GitHub Pages 프로젝트 사이트면 `/<repo>/`가 된다.
 * 앱 내부 경로(`/post/hello`)와 브라우저 실제 경로(`/<repo>/post/hello`)를 오가는 변환의 기준점이다.
 *
 * @param pathname `<base href>`의 경로 부분. 앞뒤 슬래시가 없어도 정규화된다
 */
class BasePath(
    pathname: String = ROOT,
) {
    /** 항상 `/`로 시작하고 `/`로 끝나는 정규화된 접두사. */
    val prefix: String = normalizePrefix(pathname)

    override fun toString(): String = prefix
}

/** 앞뒤 슬래시를 보정한다. 빈 문자열·공백은 루트로 본다. */
private fun normalizePrefix(pathname: String): String {
    val trimmed = pathname.trim()
    if (trimmed.isEmpty()) return ROOT
    val leading = if (trimmed.startsWith('/')) trimmed else "/$trimmed"
    return if (leading.endsWith('/')) leading else "$leading/"
}

/**
 * 브라우저 주소창 경로에서 배포 접두사를 떼어내 앱 내부 경로로 바꾼다.
 *
 * 접두사 아래가 아닌 경로는 그대로 통과시켜 `navKeyFromPath`가 404로 판정하게 둔다.
 *
 * @param locationPathname `window.location.pathname` 값
 * @return 항상 `/`로 시작하는 앱 내부 경로
 */
fun BasePath.toAppPath(locationPathname: String): String {
    val absolute = if (locationPathname.startsWith('/')) locationPathname else "/$locationPathname"
    val rest =
        when {
            absolute.startsWith(prefix) -> absolute.removePrefix(prefix)
            // 접두사에서 끝 슬래시만 빠진 형태(`/repo`)도 루트로 본다.
            "$absolute/" == prefix -> ""
            else -> absolute.removePrefix("/")
        }
    return "/$rest"
}

/**
 * 앱 내부 경로에 배포 접두사를 붙여 브라우저가 실제로 쓸 경로로 바꾼다.
 *
 * `composeResources/...`처럼 슬래시로 시작하지 않는 리소스 경로도 같은 규칙으로 처리한다.
 *
 * @param appPath `/post/hello` 또는 `composeResources/...` 형태의 경로
 * @return 접두사가 붙고 슬래시가 겹치지 않는 경로
 */
fun BasePath.toBrowserPath(appPath: String): String = prefix + appPath.removePrefix("/")
