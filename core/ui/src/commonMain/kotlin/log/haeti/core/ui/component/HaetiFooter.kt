package log.haeti.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import log.haeti.core.ui.HaetiLinks
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.horizontalPadding
import log.haeti.core.ui.platform.LocalLastUpdated
import log.haeti.core.ui.platform.rememberUrlOpener
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 페이지 하단 푸터. 구분선 아래 왼쪽에 워드마크와 마지막 갱신일, 오른쪽에 외부 링크와 RSS를 둔다.
 *
 * @param maxWidth 정렬 기준이 되는 콘텐츠 컬럼 폭
 */
@Composable
fun HaetiFooter(
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val urlOpener = rememberUrlOpener()
    Box(modifier = modifier.fillMaxWidth().padding(top = 120.dp), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = LocalLayoutTier.current.horizontalPadding),
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    HaetiWordmark()
                    LocalLastUpdated.current?.let { date ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Last updated ${date.toString().replace('-', '.')}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    val links = HaetiLinks.all(LocalHaetiStrings.current.linkEmail) + ("RSS" to HaetiLinks.RSS)
                    links.forEach { (label, url) ->
                        LinkText(text = label, onClick = { urlOpener.open(url) })
                    }
                }
            }
        }
    }
}
