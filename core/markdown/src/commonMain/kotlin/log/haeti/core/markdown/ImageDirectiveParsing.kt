package log.haeti.core.markdown

import log.haeti.core.markdown.model.ImageAlign
import log.haeti.core.markdown.model.ImageDirective

private val IMAGE_DIRECTIVE_REGEX = Regex("""!\[([^\]]*)]\(([^)\s]+)\)\{([^}]*)}""")
private val ATTRIBUTE_REGEX = Regex("""(\w+)=(?:"([^"]*)"|(\S+))""")

/**
 * 지시어 블록(`{...}`) 안의 `key=value` 속성 문자열을 [ImageDirective]로 파싱한다.
 * 값이 없거나 알 수 없는 값은 기본값으로 대체한다.
 */
internal fun parseImageDirective(attributes: String): ImageDirective {
    var width = 100
    var align = ImageAlign.CENTER
    var caption: String? = null
    var dark: String? = null
    for (match in ATTRIBUTE_REGEX.findAll(attributes)) {
        val value = match.groupValues[2].ifEmpty { match.groupValues[3] }
        when (match.groupValues[1]) {
            "width" -> width = value.toIntOrNull()?.coerceIn(10, 100) ?: width
            "align" ->
                align =
                    when (value.lowercase()) {
                        "left" -> ImageAlign.LEFT
                        "right" -> ImageAlign.RIGHT
                        else -> ImageAlign.CENTER
                    }
            "caption" -> caption = value
            "dark" -> dark = value
        }
    }
    return ImageDirective(width, align, caption, dark)
}

/**
 * 마크다운 전문에서 `![alt](path){지시어}` 패턴을 찾아 지시어 블록을 제거한 본문과
 * 경로별 [ImageDirective] 맵을 만든다. `{...}` 블록이 화면에 그대로 노출되는 것을 막기 위함이다.
 *
 * @return 지시어 블록이 제거된 마크다운과, 이미지 경로를 키로 하는 지시어 맵
 */
internal fun extractImageDirectives(markdown: String): Pair<String, Map<String, ImageDirective>> {
    val directives = mutableMapOf<String, ImageDirective>()
    val cleaned =
        IMAGE_DIRECTIVE_REGEX.replace(markdown) { match ->
            val (alt, path, attributes) = match.destructured
            directives[path] = parseImageDirective(attributes)
            "![$alt]($path)"
        }
    return cleaned to directives
}
