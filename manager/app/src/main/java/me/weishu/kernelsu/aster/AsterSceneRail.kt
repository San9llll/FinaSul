package me.weishu.kernelsu.aster

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.weishu.kernelsu.ui.component.bottombar.BottomBarDestination
import me.weishu.kernelsu.ui.component.card.LocalFolkLiquidState
import me.weishu.kernelsu.ui.component.card.folkGlassCard
import me.weishu.kernelsu.ui.theme.ContinuousCornerShape
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The Aster scene rail — the app-wide navigation column that replaces the bottom bar.
 *
 * ## Why it lives here and not on the home screen
 *
 * The rail navigates between all five pager pages, so it has to sit at the shell level next to
 * the pager, not inside any one page. The home screen draws its own content on top of the same
 * backdrop; this column is what the shell swaps the bottom bar out for.
 *
 * It reads [BottomBarDestination] rather than keeping its own list of pages, so the labels and
 * icons stay in sync with whatever the bottom bar uses — including the theme-pack overrides that
 * swap those icons out.
 *
 * It is presentational: it reports the tapped index and lets the caller drive the pager. That
 * keeps all navigation state in `MainPagerState` where the rest of the app expects it.
 */
@Composable
fun AsterSceneRail(
    selectedPage: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .width(84.dp)
            .fillMaxHeight()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsterRailClock()

        Spacer(Modifier.height(16.dp))

        BottomBarDestination.entries.forEachIndexed { index, destination ->
            RailEntry(
                label = stringResource(destination.label),
                icon = if (selectedPage == index) destination.iconSelected else destination.iconNotSelected,
                selected = selectedPage == index,
                onClick = { onSelect(index) },
            )
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.weight(1f))
    }
}

/** Ticks every twenty seconds — a minute-precision clock does not need a per-second recomposition. */
@Composable
private fun AsterRailClock() {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var text by remember { mutableStateOf(LocalTime.now().format(formatter)) }
    LaunchedEffect(Unit) {
        while (true) {
            text = LocalTime.now().format(formatter)
            delay(20_000)
        }
    }
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** One navigation entry: glyph above a label, with the active one tinted. */
@Composable
private fun RailEntry(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = ContinuousCornerShape(AsterUiConfig.corner.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .folkGlassCard(
                cornerRadius = AsterUiConfig.corner.dp,
                blurStrength = AsterUiConfig.glassStrength,
                transparency = if (selected) {
                    // The active entry stays a touch more opaque so it reads as selected even
                    // over a busy wallpaper.
                    (AsterUiConfig.glassTransparency - 0.12f).coerceAtLeast(0f)
                } else {
                    AsterUiConfig.glassTransparency
                },
                liquidState = LocalFolkLiquidState.current,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
