package org.koitharu.kotatsu.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Keep related tasks together on phones; give each a readable column when both fit. */
@Composable
internal fun ReadingPanes(leading: @Composable () -> Unit, trailing: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        if (maxWidth >= 760.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)) {
            Row(Modifier.widthIn(max = 1100.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(0.46f), horizontalAlignment = Alignment.CenterHorizontally) { leading() }
                Column(Modifier.weight(0.54f), horizontalAlignment = Alignment.CenterHorizontally) { trailing() }
            }
        } else {
            Column(Modifier.widthIn(max = 680.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                leading()
                trailing()
            }
        }
    }
}
